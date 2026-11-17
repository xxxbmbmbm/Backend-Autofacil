package cl.autofacil.vehiculos.excepcion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Convierte las excepciones en respuestas JSON con mensajes en español. */
@RestControllerAdvice
public class ManejadorGlobalExcepciones {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new RespuestaError(
                400, "Hay datos inválidos en la solicitud.", errores, LocalDateTime.now()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> cuerpoIlegible(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no tiene un formato válido.");
    }

    @ExceptionHandler(RecursoNoEncontradoExcepcion.class)
    public ResponseEntity<RespuestaError> noEncontrado(RecursoNoEncontradoExcepcion ex) {
        return responder(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ReglaNegocioExcepcion.class)
    public ResponseEntity<RespuestaError> reglaNegocio(ReglaNegocioExcepcion ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespuestaError> accesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta acción.");
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(RespuestaError.de(estado.value(), mensaje));
    }
}
