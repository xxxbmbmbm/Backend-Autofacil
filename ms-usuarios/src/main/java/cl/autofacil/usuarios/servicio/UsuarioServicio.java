package cl.autofacil.usuarios.servicio;

import cl.autofacil.usuarios.dto.UsuarioActualizarSolicitud;
import cl.autofacil.usuarios.dto.UsuarioCrearSolicitud;
import cl.autofacil.usuarios.dto.UsuarioRespuesta;
import cl.autofacil.usuarios.excepcion.RecursoNoEncontradoExcepcion;
import cl.autofacil.usuarios.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.usuarios.modelo.Rol;
import cl.autofacil.usuarios.modelo.Usuario;
import cl.autofacil.usuarios.repositorio.TokenRenovacionRepositorio;
import cl.autofacil.usuarios.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static cl.autofacil.usuarios.servicio.AutenticacionServicio.normalizarCorreo;
import static cl.autofacil.usuarios.servicio.AutenticacionServicio.textoOpcional;

/** Administración de usuarios (solo ADMIN). */
@Service
public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final TokenRenovacionRepositorio tokenRenovacionRepositorio;
    private final PasswordEncoder cifradorClaves;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio,
                           TokenRenovacionRepositorio tokenRenovacionRepositorio,
                           PasswordEncoder cifradorClaves) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.tokenRenovacionRepositorio = tokenRenovacionRepositorio;
        this.cifradorClaves = cifradorClaves;
    }

    @Transactional(readOnly = true)
    public List<UsuarioRespuesta> listar(Rol rol) {
        List<Usuario> usuarios = rol == null ? usuarioRepositorio.findAll() : usuarioRepositorio.findByRol(rol);
        return usuarios.stream().map(UsuarioRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioRespuesta obtener(Long id) {
        return UsuarioRespuesta.desde(buscar(id));
    }

    @Transactional
    public UsuarioRespuesta crear(UsuarioCrearSolicitud solicitud) {
        String correo = normalizarCorreo(solicitud.correo());
        if (usuarioRepositorio.existsByCorreo(correo)) {
            throw new ReglaNegocioExcepcion("Ya existe una cuenta con ese correo.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setClaveCifrada(cifradorClaves.encode(solicitud.clave()));
        usuario.setTelefono(textoOpcional(solicitud.telefono()));
        usuario.setRol(solicitud.rol());
        return UsuarioRespuesta.desde(usuarioRepositorio.save(usuario));
    }

    @Transactional
    public UsuarioRespuesta actualizar(Long id, UsuarioActualizarSolicitud solicitud, Long idAdministrador) {
        Usuario usuario = buscar(id);
        String correo = normalizarCorreo(solicitud.correo());
        boolean correoOcupado = usuarioRepositorio.findByCorreo(correo)
                .filter(otro -> !otro.getId().equals(id))
                .isPresent();
        if (correoOcupado) {
            throw new ReglaNegocioExcepcion("Ya existe una cuenta con ese correo.");
        }
        if (id.equals(idAdministrador) && (solicitud.rol() != Rol.ADMIN || !solicitud.activo())) {
            throw new ReglaNegocioExcepcion("No puedes quitarte el rol de administrador ni desactivar tu propia cuenta.");
        }
        if (solicitud.clave() != null && !solicitud.clave().isBlank()) {
            if (solicitud.clave().length() < 6) {
                throw new ReglaNegocioExcepcion("La contraseña debe tener al menos 6 caracteres.");
            }
            usuario.setClaveCifrada(cifradorClaves.encode(solicitud.clave()));
        }
        usuario.setNombre(solicitud.nombre().trim());
        usuario.setCorreo(correo);
        usuario.setTelefono(textoOpcional(solicitud.telefono()));
        usuario.setRol(solicitud.rol());
        usuario.setActivo(solicitud.activo());
        if (!usuario.isActivo()) {
            tokenRenovacionRepositorio.revocarTodosDelUsuario(usuario); // cierra sus sesiones abiertas
        }
        return UsuarioRespuesta.desde(usuario);
    }

    @Transactional
    public void eliminar(Long id, Long idAdministrador) {
        if (id.equals(idAdministrador)) {
            throw new ReglaNegocioExcepcion("No puedes eliminar tu propia cuenta.");
        }
        Usuario usuario = buscar(id);
        tokenRenovacionRepositorio.eliminarDelUsuario(usuario);
        usuarioRepositorio.delete(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe el usuario con id " + id + "."));
    }
}
