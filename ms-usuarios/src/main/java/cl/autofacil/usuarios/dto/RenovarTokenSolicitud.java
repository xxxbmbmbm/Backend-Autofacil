package cl.autofacil.usuarios.dto;

import jakarta.validation.constraints.NotBlank;

public record RenovarTokenSolicitud(
        @NotBlank(message = "Falta el token de renovación.")
        String tokenRenovacion
) {
}
