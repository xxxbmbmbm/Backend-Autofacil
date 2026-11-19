package cl.autofacil.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciales para iniciar sesión")
public record LoginSolicitud(
        @Schema(example = "nayafacil@autofacil.cl")
        @NotBlank(message = "Ingresa tu correo.")
        @Email(message = "Ingresa un correo electrónico válido.")
        String correo,

        @Schema(example = "********")
        @NotBlank(message = "Ingresa tu contraseña.")
        String clave
) {
}
