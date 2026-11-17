package cl.autofacil.vehiculos.controlador;

import cl.autofacil.vehiculos.dto.CambioEstadoSolicitud;
import cl.autofacil.vehiculos.dto.VehiculoRespuesta;
import cl.autofacil.vehiculos.dto.VehiculoSolicitud;
import cl.autofacil.vehiculos.modelo.EstadoVehiculo;
import cl.autofacil.vehiculos.servicio.VehiculoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vehiculos")
@Tag(name = "Vehículos", description = "Catálogo. Lectura pública; escritura solo ADMIN.")
public class VehiculoControlador {

    private final VehiculoServicio vehiculoServicio;

    public VehiculoControlador(VehiculoServicio vehiculoServicio) {
        this.vehiculoServicio = vehiculoServicio;
    }

    @GetMapping
    @Operation(summary = "Lista vehículos; todos los filtros son opcionales (la tienda pide estado=DISPONIBLE)")
    public List<VehiculoRespuesta> listar(@RequestParam(required = false) String marca,
                                          @RequestParam(required = false) Long categoriaId,
                                          @RequestParam(required = false) EstadoVehiculo estado) {
        return vehiculoServicio.listar(marca, categoriaId, estado);
    }

    @GetMapping("/marcas")
    @Operation(summary = "Marcas distintas presentes en el catálogo")
    public List<String> listarMarcas() {
        return vehiculoServicio.listarMarcas();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un vehículo")
    public VehiculoRespuesta obtener(@PathVariable Long id) {
        return vehiculoServicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agrega un vehículo", security = @SecurityRequirement(name = "bearerAuth"))
    public VehiculoRespuesta crear(@Valid @RequestBody VehiculoSolicitud solicitud) {
        return vehiculoServicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un vehículo", security = @SecurityRequirement(name = "bearerAuth"))
    public VehiculoRespuesta actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoSolicitud solicitud) {
        return vehiculoServicio.actualizar(id, solicitud);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Marca un vehículo como VENDIDO o DISPONIBLE (ADMIN o ms-ventas)",
            security = @SecurityRequirement(name = "bearerAuth"))
    public VehiculoRespuesta cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoSolicitud solicitud) {
        return vehiculoServicio.cambiarEstado(id, solicitud.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un vehículo", security = @SecurityRequirement(name = "bearerAuth"))
    public void eliminar(@PathVariable Long id) {
        vehiculoServicio.eliminar(id);
    }
}
