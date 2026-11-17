package cl.autofacil.usuarios.dto;

import cl.autofacil.usuarios.modelo.Rol;
import cl.autofacil.usuarios.modelo.Usuario;

import java.time.LocalDateTime;

/** Datos públicos de un usuario. Nunca incluye la contraseña. */
public record UsuarioRespuesta(
        Long id,
        String nombre,
        String correo,
        String telefono,
        Rol rol,
        boolean activo,
        LocalDateTime fechaCreacion
) {
    public static UsuarioRespuesta desde(Usuario usuario) {
        return new UsuarioRespuesta(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getFechaCreacion());
    }
}
