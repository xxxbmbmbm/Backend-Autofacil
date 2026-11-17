package cl.autofacil.usuarios.configuracion;

import cl.autofacil.usuarios.modelo.Rol;
import cl.autofacil.usuarios.modelo.Usuario;
import cl.autofacil.usuarios.repositorio.UsuarioRepositorio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea las cuentas iniciales de administrador y vendedor si todavía no existen.
 * Las contraseñas vienen de variables de entorno (ver .env.example); nunca quedan escritas en el frontend.
 */
@Component
public class InicializadorDatos implements CommandLineRunner {

    private static final Logger registro = LoggerFactory.getLogger(InicializadorDatos.class);

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder cifradorClaves;
    private final String correoAdmin;
    private final String claveAdmin;
    private final String correoVendedor;
    private final String claveVendedor;

    public InicializadorDatos(UsuarioRepositorio usuarioRepositorio, PasswordEncoder cifradorClaves,
                              @Value("${autofacil.inicial.admin-correo}") String correoAdmin,
                              @Value("${autofacil.inicial.admin-clave}") String claveAdmin,
                              @Value("${autofacil.inicial.vendedor-correo}") String correoVendedor,
                              @Value("${autofacil.inicial.vendedor-clave}") String claveVendedor) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.cifradorClaves = cifradorClaves;
        this.correoAdmin = correoAdmin;
        this.claveAdmin = claveAdmin;
        this.correoVendedor = correoVendedor;
        this.claveVendedor = claveVendedor;
    }

    @Override
    public void run(String... argumentos) {
        crearSiNoExiste("Administrador", correoAdmin, claveAdmin, Rol.ADMIN);
        crearSiNoExiste("Vendedor AutoFacil", correoVendedor, claveVendedor, Rol.VENDEDOR);
    }

    private void crearSiNoExiste(String nombre, String correo, String clave, Rol rol) {
        if (usuarioRepositorio.existsByCorreo(correo)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setClaveCifrada(cifradorClaves.encode(clave));
        usuario.setRol(rol);
        usuarioRepositorio.save(usuario);
        registro.info("Usuario inicial creado: {} ({})", correo, rol);
    }
}
