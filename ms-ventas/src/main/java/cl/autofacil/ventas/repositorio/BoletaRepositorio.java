package cl.autofacil.ventas.repositorio;

import cl.autofacil.ventas.modelo.Boleta;
import cl.autofacil.ventas.modelo.EstadoBoleta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoletaRepositorio extends JpaRepository<Boleta, Long> {
    List<Boleta> findAllByOrderByFechaDesc();

    List<Boleta> findByEstadoOrderByFechaDesc(EstadoBoleta estado);

    List<Boleta> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
