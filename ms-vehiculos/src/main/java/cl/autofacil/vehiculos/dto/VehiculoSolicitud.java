package cl.autofacil.vehiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para crear o actualizar un vehículo")
public record VehiculoSolicitud(
        @Schema(example = "Toyota")
        @NotBlank(message = "Ingresa la marca.")
        @Size(max = 50, message = "La marca no puede superar 50 caracteres.")
        String marca,

        @Schema(example = "Yaris")
        @NotBlank(message = "Ingresa el modelo.")
        @Size(max = 50, message = "El modelo no puede superar 50 caracteres.")
        String modelo,

        @Schema(example = "2021")
        @NotNull(message = "Ingresa el año.")
        @Min(value = 1950, message = "El año debe ser 1950 o posterior.")
        Integer anio,

        @Schema(example = "8990000")
        @NotNull(message = "Ingresa el precio.")
        @Positive(message = "El precio debe ser mayor a 0.")
        Long precio,

        @Schema(example = "9490000", description = "Precio antes de la rebaja; opcional")
        @Positive(message = "El precio anterior debe ser mayor a 0.")
        Long precioAnterior,

        @Schema(example = "32000")
        @NotNull(message = "Ingresa el kilometraje.")
        @PositiveOrZero(message = "El kilometraje no puede ser negativo.")
        Integer kilometraje,

        @Schema(example = "Automática")
        @NotBlank(message = "Selecciona la transmisión.")
        @Pattern(regexp = "Automática|Mecánica", message = "La transmisión debe ser Automática o Mecánica.")
        String transmision,

        @Schema(example = "https://commons.wikimedia.org/wiki/Special:FilePath/Chevrolet_Sail.jpg")
        @NotBlank(message = "Ingresa la URL de la imagen.")
        @Pattern(regexp = "^https?://.+", message = "La imagen debe ser una URL que empiece con http:// o https://.")
        @Size(max = 500, message = "La URL no puede superar 500 caracteres.")
        String imagen,

        @Schema(example = "#141416")
        @Pattern(regexp = "^$|^#[0-9a-fA-F]{6}$", message = "El color debe tener el formato #RRGGBB.")
        String colorFondo,

        @Schema(example = "1")
        @NotNull(message = "Selecciona la categoría.")
        Long categoriaId
) {
}
