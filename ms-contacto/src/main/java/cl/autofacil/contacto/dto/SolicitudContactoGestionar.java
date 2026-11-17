package cl.autofacil.contacto.dto;

import cl.autofacil.contacto.modelo.CanalContacto;
import cl.autofacil.contacto.modelo.EstadoSolicitud;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Lo que el administrador actualiza al atender una solicitud. */
public record SolicitudContactoGestionar(
        @NotNull(message = "Indica el estado.")
        EstadoSolicitud estado,

        CanalContacto canalRespuesta,

        @Size(max = 1000, message = "Las notas no pueden superar 1000 caracteres.")
        String notasInternas
) {
}
