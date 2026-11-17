package cl.autofacil.usuarios.controlador;

import cl.autofacil.usuarios.dto.*;
import cl.autofacil.usuarios.seguridad.UsuarioAutenticado;
import cl.autofacil.usuarios.servicio.AutenticacionServicio;
import cl.autofacil.usuarios.servicio.UsuarioServicio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y manejo de tokens JWT")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;
    private final UsuarioServicio usuarioServicio;

    public AutenticacionControlador(AutenticacionServicio autenticacionServicio, UsuarioServicio usuarioServicio) {
        this.autenticacionServicio = autenticacionServicio;
        this.usuarioServicio = usuarioServicio;
    }

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una cuenta de cliente y deja la sesión iniciada")
    public AutenticacionRespuesta registrar(@Valid @RequestBody RegistroSolicitud solicitud) {
        return autenticacionServicio.registrar(solicitud);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y entrega un token de acceso y uno de renovación")
    public AutenticacionRespuesta iniciarSesion(@Valid @RequestBody LoginSolicitud solicitud) {
        return autenticacionServicio.iniciarSesion(solicitud);
    }

    @PostMapping("/renovar")
    @Operation(summary = "Entrega tokens nuevos usando el token de renovación (el anterior queda inválido)")
    public AutenticacionRespuesta renovar(@Valid @RequestBody RenovarTokenSolicitud solicitud) {
        return autenticacionServicio.renovar(solicitud.tokenRenovacion());
    }

    @PostMapping("/cerrar-sesion")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Revoca el token de renovación")
    public void cerrarSesion(@Valid @RequestBody RenovarTokenSolicitud solicitud) {
        autenticacionServicio.cerrarSesion(solicitud.tokenRenovacion());
    }

    @GetMapping("/perfil")
    @Operation(summary = "Datos del usuario dueño del token", security = @SecurityRequirement(name = "bearerAuth"))
    public UsuarioRespuesta perfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return usuarioServicio.obtener(usuario.id());
    }
}
