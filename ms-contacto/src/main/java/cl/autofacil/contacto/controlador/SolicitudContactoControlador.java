package cl.autofacil.contacto.controlador;

import cl.autofacil.contacto.dto.ResumenBandeja;
import cl.autofacil.contacto.dto.SolicitudContactoCrear;
import cl.autofacil.contacto.dto.SolicitudContactoGestionar;
import cl.autofacil.contacto.dto.SolicitudContactoRespuesta;
import cl.autofacil.contacto.modelo.EstadoSolicitud;
import cl.autofacil.contacto.seguridad.UsuarioAutenticado;
import cl.autofacil.contacto.servicio.SolicitudContactoServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contactos")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Contacto", description = "Formulario de contacto (usuario con sesión) y bandeja de solicitudes (ADMIN)")
public class SolicitudContactoControlador {

    private final SolicitudContactoServicio servicio;

    public SolicitudContactoControlador(SolicitudContactoServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envía una solicitud de contacto (requiere sesión iniciada)")
    public SolicitudContactoRespuesta crear(@Valid @RequestBody SolicitudContactoCrear datos,
                                           @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.crear(datos, usuario);
    }

    @GetMapping("/mias")
    @Operation(summary = "Solicitudes enviadas por el usuario que inició sesión")
    public List<SolicitudContactoRespuesta> listarMias(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servicio.listarDelUsuario(usuario.id());
    }

    @GetMapping
    @Operation(summary = "Bandeja del administrador, opcionalmente filtrada por estado")
    public List<SolicitudContactoRespuesta> listar(@RequestParam(required = false) EstadoSolicitud estado) {
        return servicio.listar(estado);
    }

    @GetMapping("/resumen")
    @Operation(summary = "Contadores por estado (para el ícono de notificaciones)")
    public ResumenBandeja resumen() {
        return servicio.resumen();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de una solicitud con enlaces de WhatsApp, correo y llamada")
    public SolicitudContactoRespuesta obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Cambia el estado, registra el canal de respuesta y notas internas")
    public SolicitudContactoRespuesta gestionar(@PathVariable Long id, @Valid @RequestBody SolicitudContactoGestionar datos) {
        return servicio.gestionar(id, datos);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una solicitud")
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
