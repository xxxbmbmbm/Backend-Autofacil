package cl.autofacil.vehiculos.controlador;

import cl.autofacil.vehiculos.dto.CategoriaRespuesta;
import cl.autofacil.vehiculos.dto.CategoriaSolicitud;
import cl.autofacil.vehiculos.servicio.CategoriaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@Tag(name = "Categorías", description = "Tipos de vehículo. Lectura pública; escritura solo ADMIN.")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    @GetMapping
    @Operation(summary = "Lista las categorías")
    public List<CategoriaRespuesta> listar() {
        return categoriaServicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una categoría")
    public CategoriaRespuesta obtener(@PathVariable Long id) {
        return categoriaServicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una categoría", security = @SecurityRequirement(name = "bearerAuth"))
    public CategoriaRespuesta crear(@Valid @RequestBody CategoriaSolicitud solicitud) {
        return categoriaServicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza una categoría", security = @SecurityRequirement(name = "bearerAuth"))
    public CategoriaRespuesta actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaSolicitud solicitud) {
        return categoriaServicio.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una categoría sin vehículos", security = @SecurityRequirement(name = "bearerAuth"))
    public void eliminar(@PathVariable Long id) {
        categoriaServicio.eliminar(id);
    }
}
