package cl.autofacil.usuarios.dto;

import cl.autofacil.usuarios.modelo.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para que un administrador cree un usuario con cualquier rol")
public record UsuarioCrearSolicitud(
        @NotBlank(message = "Ingresa el nombre.")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
        String nombre,

        @NotBlank(message = "Ingresa el correo.")
        @Email(message = "Ingresa un correo electrónico válido.")
        String correo,

        @NotBlank(message = "Ingresa una contraseña.")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
        String clave,

        @Pattern(regexp = "^$|^[0-9+\s]{7,15}$", message = "Ingresa un teléfono válido.")
        String telefono,

        @Schema(example = "VENDEDOR")
        @NotNull(message = "Selecciona un rol.")
        Rol rol
) {
}
