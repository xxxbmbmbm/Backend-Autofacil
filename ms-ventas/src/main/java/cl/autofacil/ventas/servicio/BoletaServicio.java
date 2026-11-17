package cl.autofacil.ventas.servicio;

import cl.autofacil.ventas.dto.BoletaRespuesta;
import cl.autofacil.ventas.dto.BoletaSolicitud;
import cl.autofacil.ventas.dto.VehiculoRemoto;
import cl.autofacil.ventas.excepcion.RecursoNoEncontradoExcepcion;
import cl.autofacil.ventas.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.ventas.modelo.Boleta;
import cl.autofacil.ventas.modelo.DetalleBoleta;
import cl.autofacil.ventas.modelo.EstadoBoleta;
import cl.autofacil.ventas.repositorio.BoletaRepositorio;
import cl.autofacil.ventas.seguridad.UsuarioAutenticado;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class BoletaServicio {

    private final BoletaRepositorio boletaRepositorio;
    private final ClienteVehiculos clienteVehiculos;

    public BoletaServicio(BoletaRepositorio boletaRepositorio, ClienteVehiculos clienteVehiculos) {
        this.boletaRepositorio = boletaRepositorio;
        this.clienteVehiculos = clienteVehiculos;
    }

    /**
     * Confirma una compra: valida que cada vehículo exista y esté disponible, calcula el total con los
     * precios reales (nunca con precios enviados por el navegador) y marca los vehículos como VENDIDOS.
     */
    @Transactional
    public BoletaRespuesta crear(BoletaSolicitud solicitud, UsuarioAutenticado comprador) {
        if (new HashSet<>(solicitud.vehiculoIds()).size() != solicitud.vehiculoIds().size()) {
            throw new ReglaNegocioExcepcion("No puedes repetir un vehículo en la misma compra.");
        }
        Boleta boleta = new Boleta();
        boleta.setUsuarioId(comprador.id());
        boleta.setNombreCliente(comprador.nombre());
        boleta.setCorreoCliente(comprador.correo());
        boleta.setDireccion(solicitud.direccion().trim());
        boleta.setComuna(solicitud.comuna().trim());
        boleta.setCiudad(solicitud.ciudad().trim());

        long total = 0;
        for (Long vehiculoId : solicitud.vehiculoIds()) {
            VehiculoRemoto vehiculo = clienteVehiculos.obtener(vehiculoId);
            if (!vehiculo.estaDisponible()) {
                throw new ReglaNegocioExcepcion("El " + vehiculo.descripcion() + " ya no está disponible.");
            }
            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setVehiculoId(vehiculo.id());
            detalle.setDescripcion(vehiculo.descripcion());
            detalle.setPrecioUnitario(vehiculo.precio());
            detalle.setSubtotal(vehiculo.precio() * detalle.getCantidad());
            boleta.agregarDetalle(detalle);
            total += detalle.getSubtotal();
        }
        boleta.setTotal(total);
        Boleta guardada = boletaRepositorio.save(boleta);
        marcarVehiculos(solicitud.vehiculoIds());
        return BoletaRespuesta.desde(guardada);
    }

    @Transactional(readOnly = true)
    public List<BoletaRespuesta> listar(EstadoBoleta estado) {
        List<Boleta> boletas = estado == null
                ? boletaRepositorio.findAllByOrderByFechaDesc()
                : boletaRepositorio.findByEstadoOrderByFechaDesc(estado);
        return boletas.stream().map(BoletaRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<BoletaRespuesta> listarDelUsuario(Long usuarioId) {
        return boletaRepositorio.findByUsuarioIdOrderByFechaDesc(usuarioId).stream().map(BoletaRespuesta::desde).toList();
    }

    /** ADMIN y VENDEDOR ven cualquier boleta; un CLIENTE solo las suyas. */
    @Transactional(readOnly = true)
    public BoletaRespuesta obtener(Long id, UsuarioAutenticado usuario) {
        Boleta boleta = buscar(id);
        if ("CLIENTE".equals(usuario.rol()) && !boleta.getUsuarioId().equals(usuario.id())) {
            throw new AccessDeniedException("La boleta pertenece a otro cliente.");
        }
        return BoletaRespuesta.desde(boleta);
    }

    /** PAGADA → ENTREGADA o ANULADA. Al anular, los vehículos vuelven a la tienda. */
    @Transactional
    public BoletaRespuesta cambiarEstado(Long id, EstadoBoleta nuevoEstado) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() == nuevoEstado) {
            return BoletaRespuesta.desde(boleta);
        }
        if (boleta.getEstado() == EstadoBoleta.ANULADA) {
            throw new ReglaNegocioExcepcion("Una boleta anulada no puede cambiar de estado.");
        }
        if (nuevoEstado == EstadoBoleta.PAGADA) {
            throw new ReglaNegocioExcepcion("Una boleta entregada no puede volver al estado PAGADA.");
        }
        if (nuevoEstado == EstadoBoleta.ANULADA) {
            boleta.getDetalles().forEach(d -> clienteVehiculos.cambiarEstado(d.getVehiculoId(), "DISPONIBLE"));
        }
        boleta.setEstado(nuevoEstado);
        return BoletaRespuesta.desde(boleta);
    }

    @Transactional
    public void eliminar(Long id) {
        Boleta boleta = buscar(id);
        if (boleta.getEstado() != EstadoBoleta.ANULADA) {
            throw new ReglaNegocioExcepcion("Solo se pueden eliminar boletas anuladas.");
        }
        boletaRepositorio.delete(boleta);
    }

    /** Si falla a mitad de camino, devuelve a DISPONIBLE los que alcanzó a marcar (la boleta se revierte sola). */
    private void marcarVehiculos(List<Long> vehiculoIds) {
        List<Long> marcados = new ArrayList<>();
        try {
            for (Long vehiculoId : vehiculoIds) {
                clienteVehiculos.cambiarEstado(vehiculoId, "VENDIDO");
                marcados.add(vehiculoId);
            }
        } catch (RuntimeException error) {
            marcados.forEach(id -> clienteVehiculos.cambiarEstado(id, "DISPONIBLE"));
            throw error;
        }
    }

    private Boleta buscar(Long id) {
        return boletaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe la boleta con id " + id + "."));
    }
}
