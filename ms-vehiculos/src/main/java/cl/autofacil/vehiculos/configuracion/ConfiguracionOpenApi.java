package cl.autofacil.vehiculos.configuracion;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Swagger UI en /swagger-ui.html. El botón "Authorize" acepta el token de acceso. */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "AutoFacil - API de Vehículos",
        version = "v1",
        description = "Catálogo de vehículos y categorías (CRUD con roles)."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class ConfiguracionOpenApi {
}
