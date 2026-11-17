package cl.autofacil.ventas.dto;

import cl.autofacil.ventas.modelo.EstadoBoleta;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoBoletaSolicitud(
        @NotNull(message = "Indica el nuevo estado.")
        EstadoBoleta estado
) {
}
