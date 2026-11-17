package cl.autofacil.vehiculos.dto;

import cl.autofacil.vehiculos.modelo.EstadoVehiculo;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoSolicitud(
        @NotNull(message = "Indica el nuevo estado.")
        EstadoVehiculo estado
) {
}
