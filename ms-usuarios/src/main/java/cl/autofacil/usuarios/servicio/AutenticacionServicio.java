package cl.autofacil.usuarios.servicio;

import cl.autofacil.usuarios.dto.AutenticacionRespuesta;
import cl.autofacil.usuarios.dto.LoginSolicitud;
import cl.autofacil.usuarios.dto.RegistroSolicitud;
import cl.autofacil.usuarios.dto.UsuarioRespuesta;
import cl.autofacil.usuarios.excepcion.CredencialesInvalidasExcepcion;
import cl.autofacil.usuarios.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.usuarios.modelo.Rol;
import cl.autofacil.usuarios.modelo.TokenRenovacion;
import cl.autofacil.usuarios.modelo.Usuario;
import cl.autofacil.usuarios.repositorio.TokenRenovacionRepositorio;
import cl.autofacil.usuarios.repositorio.UsuarioRepositorio;
import cl.autofacil.usuarios.seguridad.JwtServicio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Registro, inicio de sesión, renovación y cierre de sesión.
 * El token de acceso dura pocos minutos; el de renovación dura días, se guarda cifrado (SHA-256)
 * y se rota en cada uso: si alguien roba uno ya usado, deja de servir.
 */
@Service
public class AutenticacionServicio {

    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos.";
    private static final SecureRandom GENERADOR_ALEATORIO = new SecureRandom();

    private final UsuarioRepositorio usuarioRepositorio;
    private final TokenRenovacionRepositorio tokenRenovacionRepositorio;
    private final PasswordEncoder cifradorClaves;
    private final JwtServicio jwtServicio;
    private final Duration duracionTokenRenovacion;

    public AutenticacionServicio(
            UsuarioRepositorio usuarioRepositorio,
            TokenRenovacionRepositorio tokenRenovacionRepositorio,
            PasswordEncoder cifradorClaves,
            JwtServicio jwtServicio,
            @Value("${autofacil.jwt.dias-renovacion}") long diasRenovacion) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tokenRenovacionRepositorio = tokenRenovacionRepositorio;
        this.cifradorClaves = cifradorClaves;
        this.jwtServicio = jwtServicio;
        this.duracionTokenRenovacion = Duration.ofDays(diasRenovacion);
    }

    @Transactional
    public AutenticacionRespuesta registrar(RegistroSolicitud solicitud) {
        String correo = normalizarCorreo(solicitud.correo());
        if (usuarioRepositorio.existsByCorreo(correo)) {
            throw new ReglaNegocioExcepcion("Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setClaveCifrada(cifradorClaves.encode(solicitud.clave()));
        usuario.setTelefono(textoOpcional(solicitud.telefono()));
        usuario.setRol(Rol.CLIENTE); // el registro público siempre crea clientes
        return emitirTokens(usuarioRepositorio.save(usuario));
    }

    @Transactional
    public AutenticacionRespuesta iniciarSesion(LoginSolicitud solicitud) {
        Usuario usuario = usuarioRepositorio.findByCorreo(normalizarCorreo(solicitud.correo()))
                .filter(Usuario::isActivo)
                .filter(u -> cifradorClaves.matches(solicitud.clave(), u.getClaveCifrada()))
                .orElseThrow(() -> new CredencialesInvalidasExcepcion(MENSAJE_CREDENCIALES));
        return emitirTokens(usuario);
    }

    @Transactional
    public AutenticacionRespuesta renovar(String tokenRenovacion) {
        TokenRenovacion guardado = tokenRenovacionRepositorio.findByToken(cifrar(tokenRenovacion))
                .filter(TokenRenovacion::estaVigente)
                .orElseThrow(() -> new CredencialesInvalidasExcepcion("La sesión expiró. Inicia sesión nuevamente."));
        Usuario usuario = guardado.getUsuario();
        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasExcepcion("La cuenta está desactivada.");
        }
        guardado.setRevocado(true); // rotación: cada token de renovación sirve una sola vez
        return emitirTokens(usuario);
    }

    @Transactional
    public void cerrarSesion(String tokenRenovacion) {
        tokenRenovacionRepositorio.findByToken(cifrar(tokenRenovacion))
                .ifPresent(token -> token.setRevocado(true));
    }

    private AutenticacionRespuesta emitirTokens(Usuario usuario) {
        byte[] bytesAleatorios = new byte[32];
        GENERADOR_ALEATORIO.nextBytes(bytesAleatorios);
        String tokenRenovacion = Base64.getUrlEncoder().withoutPadding().encodeToString(bytesAleatorios);

        TokenRenovacion registro = new TokenRenovacion();
        registro.setToken(cifrar(tokenRenovacion));
        registro.setUsuario(usuario);
        registro.setExpiraEn(Instant.now().plus(duracionTokenRenovacion));
        tokenRenovacionRepositorio.save(registro);

        return new AutenticacionRespuesta(
                jwtServicio.generarTokenAcceso(usuario),
                tokenRenovacion,
                "Bearer",
                jwtServicio.segundosDeVigencia(),
                UsuarioRespuesta.desde(usuario));
    }

    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    /** En la base de datos solo se guarda el hash del token, nunca el token original. */
    static String cifrar(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}
