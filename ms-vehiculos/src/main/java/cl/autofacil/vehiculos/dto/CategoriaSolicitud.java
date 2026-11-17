package cl.autofacil.vehiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaSolicitud(
        @Schema(example = "SUV")
        @NotBlank(message = "Ingresa el nombre de la categoría.")
        @Size(max = 50, message = "El nombre no puede superar 50 caracteres.")
        String nombre,

        @Schema(example = "Vehículos utilitarios deportivos")
        @Size(max = 255, message = "La descripción no puede superar 255 caracteres.")
        String descripcion
) {
}
