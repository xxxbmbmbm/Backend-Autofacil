package cl.autofacil.usuarios.dto;

import cl.autofacil.usuarios.modelo.Rol;
import jakarta.validation.constraints.*;

/** La contraseña es opcional: si viene vacía se mantiene la actual. */
public record UsuarioActualizarSolicitud(
        @NotBlank(message = "Ingresa el nombre.")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
        String nombre,

        @NotBlank(message = "Ingresa el correo.")
        @Email(message = "Ingresa un correo electrónico válido.")
        String correo,

        @Size(max = 72, message = "La contraseña no puede superar 72 caracteres.")
        String clave,

        @Pattern(regexp = "^$|^[0-9+\s]{7,15}$", message = "Ingresa un teléfono válido.")
        String telefono,

        @NotNull(message = "Selecciona un rol.")
        Rol rol,

        boolean activo
) {
}
