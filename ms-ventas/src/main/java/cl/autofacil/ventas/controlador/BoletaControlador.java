package cl.autofacil.ventas.controlador;

import cl.autofacil.ventas.dto.BoletaRespuesta;
import cl.autofacil.ventas.dto.BoletaSolicitud;
import cl.autofacil.ventas.dto.CambioEstadoBoletaSolicitud;
import cl.autofacil.ventas.modelo.EstadoBoleta;
import cl.autofacil.ventas.seguridad.UsuarioAutenticado;
import cl.autofacil.ventas.servicio.BoletaServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Boletas", description = "Órdenes de compra. CLIENTE compra; ADMIN y VENDEDOR consultan; solo ADMIN modifica.")
public class BoletaControlador {

    private final BoletaServicio boletaServicio;

    public BoletaControlador(BoletaServicio boletaServicio) {
        this.boletaServicio = boletaServicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Confirma la compra de uno o más vehículos (CLIENTE o ADMIN)")
    public BoletaRespuesta crear(@Valid @RequestBody BoletaSolicitud solicitud,
                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return boletaServicio.crear(solicitud, usuario);
    }

    @GetMapping
    @Operation(summary = "Lista todas las boletas (ADMIN o VENDEDOR)")
    public List<BoletaRespuesta> listar(@RequestParam(required = false) EstadoBoleta estado) {
        return boletaServicio.listar(estado);
    }

    @GetMapping("/mias")
    @Operation(summary = "Boletas del usuario que inició sesión")
    public List<BoletaRespuesta> listarMias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return boletaServicio.listarDelUsuario(usuario.id());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una boleta (un CLIENTE solo puede ver las suyas)")
    public BoletaRespuesta obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return boletaServicio.obtener(id, usuario);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Marca la boleta como ENTREGADA o ANULADA (ADMIN)")
    public BoletaRespuesta cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoBoletaSolicitud solicitud) {
        return boletaServicio.cambiarEstado(id, solicitud.estado());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una boleta anulada (ADMIN)")
    public void eliminar(@PathVariable Long id) {
        boletaServicio.eliminar(id);
    }
}
