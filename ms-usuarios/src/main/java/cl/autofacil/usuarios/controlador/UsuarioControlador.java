package cl.autofacil.usuarios.controlador;

import cl.autofacil.usuarios.dto.UsuarioActualizarSolicitud;
import cl.autofacil.usuarios.dto.UsuarioCrearSolicitud;
import cl.autofacil.usuarios.dto.UsuarioRespuesta;
import cl.autofacil.usuarios.modelo.Rol;
import cl.autofacil.usuarios.seguridad.UsuarioAutenticado;
import cl.autofacil.usuarios.servicio.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "CRUD de usuarios. Solo ADMIN.")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    @Operation(summary = "Lista los usuarios, opcionalmente filtrados por rol")
    public List<UsuarioRespuesta> listar(@RequestParam(required = false) Rol rol) {
        return usuarioServicio.listar(rol);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id")
    public UsuarioRespuesta obtener(@PathVariable Long id) {
        return usuarioServicio.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea un usuario con cualquier rol (por ejemplo, un vendedor)")
    public UsuarioRespuesta crear(@Valid @RequestBody UsuarioCrearSolicitud solicitud) {
        return usuarioServicio.crear(solicitud);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza un usuario; la contraseña es opcional")
    public UsuarioRespuesta actualizar(@PathVariable Long id,
                                       @Valid @RequestBody UsuarioActualizarSolicitud solicitud,
                                       @AuthenticationPrincipal UsuarioAutenticado administrador) {
        return usuarioServicio.actualizar(id, solicitud, administrador.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un usuario (no puedes eliminarte a ti mismo)")
    public void eliminar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado administrador) {
        usuarioServicio.eliminar(id, administrador.id());
    }
}
