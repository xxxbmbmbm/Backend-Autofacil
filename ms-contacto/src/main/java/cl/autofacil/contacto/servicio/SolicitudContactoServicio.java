package cl.autofacil.contacto.servicio;

import cl.autofacil.contacto.dto.ResumenBandeja;
import cl.autofacil.contacto.dto.SolicitudContactoCrear;
import cl.autofacil.contacto.dto.SolicitudContactoGestionar;
import cl.autofacil.contacto.dto.SolicitudContactoRespuesta;
import cl.autofacil.contacto.excepcion.RecursoNoEncontradoExcepcion;
import cl.autofacil.contacto.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.contacto.modelo.EstadoSolicitud;
import cl.autofacil.contacto.modelo.SolicitudContacto;
import cl.autofacil.contacto.repositorio.SolicitudContactoRepositorio;
import cl.autofacil.contacto.seguridad.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitudContactoServicio {

    private final SolicitudContactoRepositorio repositorio;

    public SolicitudContactoServicio(SolicitudContactoRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public SolicitudContactoRespuesta crear(SolicitudContactoCrear datos, UsuarioAutenticado usuario) {
        SolicitudContacto solicitud = new SolicitudContacto();
        solicitud.setUsuarioId(usuario.id());
        solicitud.setNombre(datos.nombre().trim());
        solicitud.setCorreo(datos.correo().trim().toLowerCase());
        solicitud.setTelefono(datos.telefono() == null || datos.telefono().isBlank() ? null : datos.telefono().trim());
        solicitud.setAsunto(datos.asunto());
        solicitud.setMensaje(datos.mensaje().trim());
        solicitud.setVehiculoId(datos.vehiculoId());
        return SolicitudContactoRespuesta.paraCliente(repositorio.save(solicitud));
    }

    @Transactional(readOnly = true)
    public List<SolicitudContactoRespuesta> listarDelUsuario(Long usuarioId) {
        return repositorio.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId).stream()
                .map(SolicitudContactoRespuesta::paraCliente).toList();
    }

    @Transactional(readOnly = true)
    public List<SolicitudContactoRespuesta> listar(EstadoSolicitud estado) {
        List<SolicitudContacto> solicitudes = estado == null
                ? repositorio.findAllByOrderByFechaCreacionDesc()
                : repositorio.findByEstadoOrderByFechaCreacionDesc(estado);
        return solicitudes.stream().map(SolicitudContactoRespuesta::paraAdministrador).toList();
    }

    @Transactional(readOnly = true)
    public ResumenBandeja resumen() {
        return new ResumenBandeja(
                repositorio.countByEstado(EstadoSolicitud.NUEVA),
                repositorio.countByEstado(EstadoSolicitud.EN_PROCESO),
                repositorio.countByEstado(EstadoSolicitud.RESPONDIDA),
                repositorio.countByEstado(EstadoSolicitud.CERRADA));
    }

    @Transactional(readOnly = true)
    public SolicitudContactoRespuesta obtener(Long id) {
        return SolicitudContactoRespuesta.paraAdministrador(buscar(id));
    }

    /** Para marcar RESPONDIDA hay que indicar por qué canal se contactó al cliente. */
    @Transactional
    public SolicitudContactoRespuesta gestionar(Long id, SolicitudContactoGestionar datos) {
        SolicitudContacto solicitud = buscar(id);
        if (datos.estado() == EstadoSolicitud.RESPONDIDA && datos.canalRespuesta() == null) {
            throw new ReglaNegocioExcepcion("Indica por qué medio respondiste: WhatsApp, correo o llamada.");
        }
        if (datos.estado() == EstadoSolicitud.NUEVA && solicitud.getEstado() != EstadoSolicitud.NUEVA) {
            throw new ReglaNegocioExcepcion("Una solicitud ya atendida no puede volver a quedar como nueva.");
        }
        solicitud.setEstado(datos.estado());
        if (datos.canalRespuesta() != null) solicitud.setCanalRespuesta(datos.canalRespuesta());
        if (datos.notasInternas() != null) solicitud.setNotasInternas(datos.notasInternas().trim());
        return SolicitudContactoRespuesta.paraAdministrador(repositorio.saveAndFlush(solicitud));
    }

    @Transactional
    public void eliminar(Long id) {
        repositorio.delete(buscar(id));
    }

    private SolicitudContacto buscar(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe la solicitud con id " + id + "."));
    }
}
