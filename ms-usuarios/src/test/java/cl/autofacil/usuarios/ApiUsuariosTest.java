package cl.autofacil.usuarios;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración: levantan la aplicación completa (seguridad, controladores, servicios y
 * una base H2 en memoria) y recorren los flujos reales de la API.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiUsuariosTest {

    @Autowired
    private MockMvc api;

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String json, String token) throws Exception {
        if (json != null) peticion.contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) peticion.header("Authorization", "Bearer " + token);
        return api.perform(peticion);
    }

    private String login(String correo, String clave) throws Exception {
        String cuerpo = enviar(post("/api/v1/auth/login"), "{\"correo\":\"%s\",\"clave\":\"%s\"}".formatted(correo, clave), null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.tokenAcceso");
    }

    @Test
    void clienteSeRegistraRenuevaTokenYCierraSesion() throws Exception {
        String registro = """
                {"nombre":"Ana Pérez","correo":"Ana@Correo.cl","clave":"Clave123","telefono":"+56 9 1111 2222"}""";
        String cuerpo = enviar(post("/api/v1/auth/registro"), registro, null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.usuario.correo").value("ana@correo.cl"))
                .andReturn().getResponse().getContentAsString();
        String tokenAcceso = JsonPath.read(cuerpo, "$.tokenAcceso");
        String renovacion = JsonPath.read(cuerpo, "$.tokenRenovacion");

        enviar(get("/api/v1/auth/perfil"), null, tokenAcceso)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana Pérez"));

        // Correo repetido
        enviar(post("/api/v1/auth/registro"), registro, null).andExpect(status().isConflict());

        // Renovar entrega tokens nuevos y el anterior deja de servir (rotación)
        String json = "{\"tokenRenovacion\":\"" + renovacion + "\"}";
        String renovado = enviar(post("/api/v1/auth/renovar"), json, null)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        enviar(post("/api/v1/auth/renovar"), json, null).andExpect(status().isUnauthorized());

        // Cerrar sesión revoca el token de renovación vigente
        String jsonNuevo = "{\"tokenRenovacion\":\"" + JsonPath.read(renovado, "$.tokenRenovacion") + "\"}";
        enviar(post("/api/v1/auth/cerrar-sesion"), jsonNuevo, null).andExpect(status().isNoContent());
        enviar(post("/api/v1/auth/renovar"), jsonNuevo, null).andExpect(status().isUnauthorized());

        // Un cliente no puede administrar usuarios
        enviar(get("/api/v1/usuarios"), null, tokenAcceso).andExpect(status().isForbidden());
    }

    @Test
    void rechazaCredencialesDatosYTokensInvalidos() throws Exception {
        enviar(post("/api/v1/auth/login"), "{\"correo\":\"admin@autofacil.cl\",\"clave\":\"mala\"}", null)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos."));
        enviar(post("/api/v1/auth/registro"), "{\"nombre\":\"A\",\"correo\":\"no-es-correo\",\"clave\":\"1\"}", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.correo").exists());
        enviar(post("/api/v1/auth/login"), "{esto no es json", null).andExpect(status().isBadRequest());
        enviar(get("/api/v1/auth/perfil"), null, null).andExpect(status().isUnauthorized());
        enviar(get("/api/v1/auth/perfil"), null, "token.falso.123").andExpect(status().isUnauthorized());
    }

    @Test
    void administradorGestionaUsuarios() throws Exception {
        String admin = login("admin@autofacil.cl", "Admin@2026");

        String creado = enviar(post("/api/v1/usuarios"),
                "{\"nombre\":\"Pedro\",\"correo\":\"pedro@autofacil.cl\",\"clave\":\"Clave123\",\"rol\":\"VENDEDOR\"}", admin)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creado, "$.id");

        enviar(get("/api/v1/usuarios").param("rol", "VENDEDOR"), null, admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)); // el vendedor inicial + Pedro
        enviar(get("/api/v1/usuarios/" + id), null, admin).andExpect(jsonPath("$.nombre").value("Pedro"));

        enviar(put("/api/v1/usuarios/" + id),
                "{\"nombre\":\"Pedro Soto\",\"correo\":\"pedro@autofacil.cl\",\"clave\":\"Nueva123\",\"rol\":\"VENDEDOR\",\"activo\":false}", admin)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        // Usuario desactivado no puede entrar
        enviar(post("/api/v1/auth/login"), "{\"correo\":\"pedro@autofacil.cl\",\"clave\":\"Nueva123\"}", null)
                .andExpect(status().isUnauthorized());

        // Reglas de negocio: correo ocupado, clave corta, quitarse el rol propio
        enviar(put("/api/v1/usuarios/" + id),
                "{\"nombre\":\"Pedro\",\"correo\":\"admin@autofacil.cl\",\"rol\":\"VENDEDOR\",\"activo\":true}", admin)
                .andExpect(status().isConflict());
        enviar(put("/api/v1/usuarios/" + id),
                "{\"nombre\":\"Pedro\",\"correo\":\"pedro@autofacil.cl\",\"clave\":\"123\",\"rol\":\"VENDEDOR\",\"activo\":true}", admin)
                .andExpect(status().isConflict());
        Integer idAdmin = JsonPath.read(enviar(get("/api/v1/auth/perfil"), null, admin)
                .andReturn().getResponse().getContentAsString(), "$.id");
        enviar(put("/api/v1/usuarios/" + idAdmin),
                "{\"nombre\":\"Admin\",\"correo\":\"admin@autofacil.cl\",\"rol\":\"CLIENTE\",\"activo\":true}", admin)
                .andExpect(status().isConflict());
        enviar(delete("/api/v1/usuarios/" + idAdmin), null, admin).andExpect(status().isConflict());
        enviar(post("/api/v1/usuarios"),
                "{\"nombre\":\"Otro\",\"correo\":\"pedro@autofacil.cl\",\"clave\":\"Clave123\",\"rol\":\"CLIENTE\"}", admin)
                .andExpect(status().isConflict());

        enviar(delete("/api/v1/usuarios/" + id), null, admin).andExpect(status().isNoContent());
        enviar(get("/api/v1/usuarios/" + id), null, admin).andExpect(status().isNotFound());
    }
}
