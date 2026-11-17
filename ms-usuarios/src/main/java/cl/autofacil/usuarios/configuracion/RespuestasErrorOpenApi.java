package cl.autofacil.usuarios.configuracion;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agrega a cada endpoint de Swagger las respuestas de error que realmente puede devolver,
 * según lo que recibe: datos (400), token (401/403), un {id} (404) o si modifica información (409).
 * Todas usan el mismo formato JSON de RespuestaError.
 */
@Configuration
public class RespuestasErrorOpenApi {

    @Bean
    public OperationCustomizer respuestasDeError() {
        return (operacion, metodo) -> {
            ApiResponses respuestas = operacion.getResponses();
            String ruta = metodo.getMethod().getDeclaringClass().getSimpleName() + "." + metodo.getMethod().getName();
            boolean recibeDatos = operacion.getRequestBody() != null || tieneParametros(operacion);
            boolean requiereToken = operacion.getSecurity() != null && !operacion.getSecurity().isEmpty();
            boolean recibeId = operacion.getParameters() != null
                    && operacion.getParameters().stream().anyMatch(p -> "id".equals(p.getName()));
            boolean modifica = operacion.getRequestBody() != null || ruta.contains(".eliminar");

            if (recibeDatos) agregar(respuestas, "400", "Datos inválidos: el cuerpo trae 'errores' con el mensaje de cada campo.");
            if (requiereToken || ruta.contains("Autenticacion")) {
                agregar(respuestas, "401", "Sin token, token vencido o credenciales incorrectas.");
            }
            if (requiereToken) agregar(respuestas, "403", "El rol del token no tiene permiso para esta acción.");
            if (recibeId) agregar(respuestas, "404", "No existe un registro con ese id.");
            if (modifica) agregar(respuestas, "409", "Se rompe una regla de negocio (por ejemplo, un dato repetido o un estado no permitido).");
            return operacion;
        };
    }

    private static boolean tieneParametros(Operation operacion) {
        return operacion.getParameters() != null && !operacion.getParameters().isEmpty();
    }

    private static void agregar(ApiResponses respuestas, String codigo, String descripcion) {
        respuestas.putIfAbsent(codigo, new ApiResponse().description(descripcion));
    }
}
