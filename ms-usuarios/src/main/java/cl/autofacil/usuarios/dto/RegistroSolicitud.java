package cl.autofacil.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para que un cliente cree su cuenta")
public record RegistroSolicitud(
        @Schema(example = "Ana Pérez")
        @NotBlank(message = "Ingresa tu nombre.")
        @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres.")
        String nombre,

        @Schema(example = "ana@correo.cl")
        @NotBlank(message = "Ingresa tu correo.")
        @Email(message = "Ingresa un correo electrónico válido.")
        String correo,

        @Schema(example = "Clave123")
        @NotBlank(message = "Ingresa una contraseña.")
        @Size(min = 6, max = 72, message = "La contraseña debe tener entre 6 y 72 caracteres.")
        String clave,

        @Schema(example = "+56 9 1234 5678")
        @Pattern(regexp = "^$|^[0-9+\s]{7,15}$", message = "Ingresa un teléfono válido.")
        String telefono
) {
}
