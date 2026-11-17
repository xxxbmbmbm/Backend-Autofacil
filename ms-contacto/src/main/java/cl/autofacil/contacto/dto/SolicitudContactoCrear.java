package cl.autofacil.contacto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos del formulario de contacto")
public record SolicitudContactoCrear(
        @Schema(example = "Ana Pérez")
        @NotBlank(message = "Ingresa tu nombre.")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
        String nombre,

        @Schema(example = "ana@correo.cl")
        @NotBlank(message = "Ingresa tu correo.")
        @Email(message = "Ingresa un correo electrónico válido.")
        String correo,

        @Schema(example = "+56 9 1234 5678")
        @Pattern(regexp = "^$|^[0-9+\\s]{7,15}$", message = "Ingresa un teléfono válido.")
        String telefono,

        @Schema(example = "Consulta por un vehículo")
        @NotBlank(message = "Selecciona un asunto.")
        @Pattern(regexp = "Consulta por un vehículo|Quiero vender mi vehículo|Consulta general|Otro",
                message = "Selecciona un asunto de la lista.")
        String asunto,

        @Schema(example = "Hola, ¿el Toyota Yaris sigue disponible?")
        @NotBlank(message = "Cuéntanos en qué te podemos ayudar.")
        @Size(min = 10, max = 1000, message = "El mensaje debe tener entre 10 y 1000 caracteres.")
        String mensaje,

        @Schema(example = "1", description = "Opcional: vehículo por el que se consulta")
        Long vehiculoId
) {
}
