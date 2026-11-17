package cl.autofacil.contacto.repositorio;

import cl.autofacil.contacto.modelo.EstadoSolicitud;
import cl.autofacil.contacto.modelo.SolicitudContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolicitudContactoRepositorio extends JpaRepository<SolicitudContacto, Long> {
    List<SolicitudContacto> findAllByOrderByFechaCreacionDesc();

    List<SolicitudContacto> findByEstadoOrderByFechaCreacionDesc(EstadoSolicitud estado);

    List<SolicitudContacto> findByUsuarioIdOrderByFechaCreacionDesc(Long usuarioId);

    long countByEstado(EstadoSolicitud estado);
}
