package cl.autofacil.contacto.dto;

import cl.autofacil.contacto.modelo.CanalContacto;
import cl.autofacil.contacto.modelo.EstadoSolicitud;
import cl.autofacil.contacto.modelo.SolicitudContacto;
import cl.autofacil.contacto.servicio.EnlacesContacto;

import java.time.LocalDateTime;

public record SolicitudContactoRespuesta(
        Long id,
        Long usuarioId,
        String nombre,
        String correo,
        String telefono,
        String asunto,
        String mensaje,
        Long vehiculoId,
        EstadoSolicitud estado,
        CanalContacto canalRespuesta,
        String notasInternas,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        Enlaces enlaces
) {
    /** Enlaces listos para usar en un botón: abren WhatsApp, el correo o el marcador del teléfono. */
    public record Enlaces(String whatsapp, String correo, String llamada) {
    }

    /** Vista del administrador: incluye notas internas y enlaces de contacto. */
    public static SolicitudContactoRespuesta paraAdministrador(SolicitudContacto s) {
        Enlaces enlaces = new Enlaces(
                EnlacesContacto.whatsapp(s.getTelefono(), s.getNombre(), s.getAsunto()),
                EnlacesContacto.correo(s.getCorreo(), s.getAsunto()),
                EnlacesContacto.llamada(s.getTelefono()));
        return new SolicitudContactoRespuesta(s.getId(), s.getUsuarioId(), s.getNombre(), s.getCorreo(), s.getTelefono(),
                s.getAsunto(), s.getMensaje(), s.getVehiculoId(), s.getEstado(), s.getCanalRespuesta(),
                s.getNotasInternas(), s.getFechaCreacion(), s.getFechaActualizacion(), enlaces);
    }

    /** Vista del cliente: sin notas internas ni enlaces. */
    public static SolicitudContactoRespuesta paraCliente(SolicitudContacto s) {
        return new SolicitudContactoRespuesta(s.getId(), s.getUsuarioId(), s.getNombre(), s.getCorreo(), s.getTelefono(),
                s.getAsunto(), s.getMensaje(), s.getVehiculoId(), s.getEstado(), s.getCanalRespuesta(),
                null, s.getFechaCreacion(), s.getFechaActualizacion(), null);
    }
}
