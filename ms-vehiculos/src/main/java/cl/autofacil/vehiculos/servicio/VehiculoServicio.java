package cl.autofacil.vehiculos.servicio;

import cl.autofacil.vehiculos.dto.VehiculoRespuesta;
import cl.autofacil.vehiculos.dto.VehiculoSolicitud;
import cl.autofacil.vehiculos.excepcion.RecursoNoEncontradoExcepcion;
import cl.autofacil.vehiculos.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.vehiculos.modelo.EstadoVehiculo;
import cl.autofacil.vehiculos.modelo.Vehiculo;
import cl.autofacil.vehiculos.repositorio.VehiculoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
public class VehiculoServicio {

    private final VehiculoRepositorio vehiculoRepositorio;
    private final CategoriaServicio categoriaServicio;

    public VehiculoServicio(VehiculoRepositorio vehiculoRepositorio, CategoriaServicio categoriaServicio) {
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.categoriaServicio = categoriaServicio;
    }

    @Transactional(readOnly = true)
    public List<VehiculoRespuesta> listar(String marca, Long categoriaId, EstadoVehiculo estado) {
        String marcaFiltro = marca == null || marca.isBlank() ? null : marca.trim();
        return vehiculoRepositorio.buscar(marcaFiltro, categoriaId, estado).stream()
                .map(VehiculoRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<String> listarMarcas() {
        return vehiculoRepositorio.listarMarcas();
    }

    @Transactional(readOnly = true)
    public VehiculoRespuesta obtener(Long id) {
        return VehiculoRespuesta.desde(buscar(id));
    }

    @Transactional
    public VehiculoRespuesta crear(VehiculoSolicitud solicitud) {
        Vehiculo vehiculo = new Vehiculo();
        copiarDatos(solicitud, vehiculo);
        return VehiculoRespuesta.desde(vehiculoRepositorio.save(vehiculo));
    }

    @Transactional
    public VehiculoRespuesta actualizar(Long id, VehiculoSolicitud solicitud) {
        Vehiculo vehiculo = buscar(id);
        copiarDatos(solicitud, vehiculo);
        return VehiculoRespuesta.desde(vehiculo);
    }

    /** Lo usa ms-ventas al confirmar una compra (VENDIDO) o al anularla (DISPONIBLE). */
    @Transactional
    public VehiculoRespuesta cambiarEstado(Long id, EstadoVehiculo nuevoEstado) {
        Vehiculo vehiculo = buscar(id);
        if (nuevoEstado == EstadoVehiculo.VENDIDO && vehiculo.getEstado() == EstadoVehiculo.VENDIDO) {
            throw new ReglaNegocioExcepcion("El " + vehiculo.getMarca() + " " + vehiculo.getModelo() + " ya fue vendido.");
        }
        vehiculo.setEstado(nuevoEstado);
        return VehiculoRespuesta.desde(vehiculo);
    }

    @Transactional
    public void eliminar(Long id) {
        vehiculoRepositorio.delete(buscar(id));
    }

    private Vehiculo buscar(Long id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe el vehículo con id " + id + "."));
    }

    private void copiarDatos(VehiculoSolicitud solicitud, Vehiculo vehiculo) {
        int anioMaximo = Year.now().getValue() + 1;
        if (solicitud.anio() > anioMaximo) {
            throw new ReglaNegocioExcepcion("El año no puede ser mayor a " + anioMaximo + ".");
        }
        if (solicitud.precioAnterior() != null && solicitud.precioAnterior() <= solicitud.precio()) {
            throw new ReglaNegocioExcepcion("El precio anterior debe ser mayor al precio actual para mostrarse como oferta.");
        }
        vehiculo.setMarca(solicitud.marca().trim());
        vehiculo.setModelo(solicitud.modelo().trim());
        vehiculo.setAnio(solicitud.anio());
        vehiculo.setPrecio(solicitud.precio());
        vehiculo.setPrecioAnterior(solicitud.precioAnterior());
        vehiculo.setKilometraje(solicitud.kilometraje());
        vehiculo.setTransmision(solicitud.transmision());
        vehiculo.setImagen(solicitud.imagen().trim());
        vehiculo.setColorFondo(solicitud.colorFondo() == null || solicitud.colorFondo().isBlank() ? "#141416" : solicitud.colorFondo());
        vehiculo.setCategoria(categoriaServicio.buscar(solicitud.categoriaId()));
    }
}
