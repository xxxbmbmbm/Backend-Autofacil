package cl.autofacil.contacto.excepcion;

import java.time.LocalDateTime;
import java.util.Map;

/** Formato único de error que recibe el frontend. */
public record RespuestaError(
        int estado,
        String mensaje,
        Map<String, String> errores,
        LocalDateTime fecha
) {
    public static RespuestaError de(int estado, String mensaje) {
        return new RespuestaError(estado, mensaje, Map.of(), LocalDateTime.now());
    }
}
