package cl.autofacil.ventas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Datos de la compra. No incluye datos de tarjeta: el pago del frontend es solo demostrativo
 * y el backend nunca recibe ni guarda números de tarjeta.
 */
@Schema(description = "Compra de uno o más vehículos")
public record BoletaSolicitud(
        @Schema(example = "[3]")
        @NotEmpty(message = "Selecciona al menos un vehículo.")
        @Size(max = 5, message = "Puedes comprar como máximo 5 vehículos por boleta.")
        List<Long> vehiculoIds,

        @Schema(example = "Av. Providencia 1234, depto 56")
        @NotBlank(message = "Ingresa tu calle y número.")
        @Size(max = 150, message = "La dirección no puede superar 150 caracteres.")
        String direccion,

        @Schema(example = "Providencia")
        @NotBlank(message = "Ingresa tu comuna.")
        @Size(max = 80, message = "La comuna no puede superar 80 caracteres.")
        String comuna,

        @Schema(example = "Santiago")
        @NotBlank(message = "Ingresa tu ciudad.")
        @Size(max = 80, message = "La ciudad no puede superar 80 caracteres.")
        String ciudad
) {
}
