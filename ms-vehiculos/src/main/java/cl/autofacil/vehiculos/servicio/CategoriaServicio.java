package cl.autofacil.vehiculos.servicio;

import cl.autofacil.vehiculos.dto.CategoriaRespuesta;
import cl.autofacil.vehiculos.dto.CategoriaSolicitud;
import cl.autofacil.vehiculos.excepcion.RecursoNoEncontradoExcepcion;
import cl.autofacil.vehiculos.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.vehiculos.modelo.Categoria;
import cl.autofacil.vehiculos.repositorio.CategoriaRepositorio;
import cl.autofacil.vehiculos.repositorio.VehiculoRepositorio;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaServicio {

    private final CategoriaRepositorio categoriaRepositorio;
    private final VehiculoRepositorio vehiculoRepositorio;

    public CategoriaServicio(CategoriaRepositorio categoriaRepositorio, VehiculoRepositorio vehiculoRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<CategoriaRespuesta> listar() {
        return categoriaRepositorio.findAll(Sort.by("nombre")).stream().map(CategoriaRespuesta::desde).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaRespuesta obtener(Long id) {
        return CategoriaRespuesta.desde(buscar(id));
    }

    @Transactional
    public CategoriaRespuesta crear(CategoriaSolicitud solicitud) {
        validarNombreLibre(solicitud.nombre(), null);
        Categoria categoria = new Categoria(solicitud.nombre().trim(), solicitud.descripcion());
        return CategoriaRespuesta.desde(categoriaRepositorio.save(categoria));
    }

    @Transactional
    public CategoriaRespuesta actualizar(Long id, CategoriaSolicitud solicitud) {
        Categoria categoria = buscar(id);
        validarNombreLibre(solicitud.nombre(), id);
        categoria.setNombre(solicitud.nombre().trim());
        categoria.setDescripcion(solicitud.descripcion());
        return CategoriaRespuesta.desde(categoria);
    }

    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = buscar(id);
        if (vehiculoRepositorio.existsByCategoriaId(id)) {
            throw new ReglaNegocioExcepcion("No se puede eliminar una categoría que tiene vehículos asociados.");
        }
        categoriaRepositorio.delete(categoria);
    }

    Categoria buscar(Long id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe la categoría con id " + id + "."));
    }

    private void validarNombreLibre(String nombre, Long idActual) {
        categoriaRepositorio.findByNombreIgnoreCase(nombre.trim())
                .filter(otra -> !otra.getId().equals(idActual))
                .ifPresent(otra -> {
                    throw new ReglaNegocioExcepcion("Ya existe una categoría llamada " + otra.getNombre() + ".");
                });
    }
}
