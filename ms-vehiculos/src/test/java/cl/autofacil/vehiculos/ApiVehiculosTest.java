package cl.autofacil.vehiculos;

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

import static cl.autofacil.vehiculos.TokensPrueba.token;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración sobre la API real con H2 y el catálogo inicial cargado. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiVehiculosTest {

    private static final String ADMIN = token(1, "ADMIN");

    @Autowired
    private MockMvc api;

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String json, String token) throws Exception {
        if (json != null) peticion.contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) peticion.header("Authorization", "Bearer " + token);
        return api.perform(peticion);
    }

    private static String vehiculo(Object categoriaId, long precio, Long precioAnterior, int anio) {
        return """
                {"marca":"BYD","modelo":"Dolphin","anio":%d,"precio":%d,"precioAnterior":%s,"kilometraje":0,
                 "transmision":"Automática","imagen":"https://ejemplo.cl/byd.jpg","colorFondo":"","categoriaId":%s}"""
                .formatted(anio, precio, precioAnterior, categoriaId);
    }

    @Test
    void cualquieraConsultaElCatalogo() throws Exception {
        enviar(get("/api/v1/vehiculos"), null, null).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].marca").value("Toyota")) // mismo orden que tenía el frontend
                .andExpect(jsonPath("$[0].tipo").value("Hatchback"));
        enviar(get("/api/v1/vehiculos").param("marca", "toyota").param("estado", "DISPONIBLE"), null, null)
                .andExpect(jsonPath("$.length()").value(2));
        enviar(get("/api/v1/vehiculos").param("marca", " ").param("categoriaId", "3"), null, null)
                .andExpect(jsonPath("$[0].tipo").value("SUV"));
        enviar(get("/api/v1/vehiculos/marcas"), null, null).andExpect(jsonPath("$.length()").value(10));
        enviar(get("/api/v1/vehiculos/1"), null, null).andExpect(jsonPath("$.modelo").value("Vento"));
        enviar(get("/api/v1/vehiculos/999"), null, null).andExpect(status().isNotFound());
        enviar(get("/api/v1/categorias"), null, null).andExpect(jsonPath("$.length()").value(4));
        enviar(get("/api/v1/categorias/1"), null, null).andExpect(jsonPath("$.nombre").value("Hatchback"));
    }

    @Test
    void soloElAdministradorModificaElCatalogo() throws Exception {
        String categoria = "{\"nombre\":\"Eléctrico\",\"descripcion\":\"Cero emisiones\"}";
        enviar(post("/api/v1/categorias"), categoria, null).andExpect(status().isUnauthorized());
        enviar(post("/api/v1/categorias"), categoria, token(2, "CLIENTE")).andExpect(status().isForbidden());
        enviar(post("/api/v1/categorias"), categoria, token(3, "VENDEDOR")).andExpect(status().isForbidden());

        Integer idCategoria = JsonPath.read(enviar(post("/api/v1/categorias"), categoria, ADMIN)
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), "$.id");
        enviar(post("/api/v1/categorias"), "{\"nombre\":\"suv\"}", ADMIN).andExpect(status().isConflict());
        enviar(put("/api/v1/categorias/" + idCategoria), "{\"nombre\":\"Eléctricos\"}", ADMIN)
                .andExpect(jsonPath("$.nombre").value("Eléctricos"));

        // Validaciones y reglas de negocio
        enviar(post("/api/v1/vehiculos"), "{\"marca\":\"\"}", ADMIN)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.marca").exists());
        enviar(post("/api/v1/vehiculos"), vehiculo(idCategoria, 9_000_000, 8_000_000L, 2024), ADMIN).andExpect(status().isConflict());
        enviar(post("/api/v1/vehiculos"), vehiculo(idCategoria, 9_000_000, null, 2999), ADMIN).andExpect(status().isConflict());
        enviar(post("/api/v1/vehiculos"), vehiculo(999, 9_000_000, null, 2024), ADMIN).andExpect(status().isNotFound());

        Integer id = JsonPath.read(enviar(post("/api/v1/vehiculos"), vehiculo(idCategoria, 20_000_000, null, 2024), ADMIN)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.colorFondo").value("#141416"))
                .andReturn().getResponse().getContentAsString(), "$.id");
        enviar(put("/api/v1/vehiculos/" + id), vehiculo(idCategoria, 18_000_000, 20_000_000L, 2024), ADMIN)
                .andExpect(jsonPath("$.precioAnterior").value(20_000_000));

        // ms-ventas (rol SERVICIO) marca el vehículo como vendido; no se puede vender dos veces
        String vendido = "{\"estado\":\"VENDIDO\"}";
        enviar(patch("/api/v1/vehiculos/" + id + "/estado"), vendido, token(0, "SERVICIO"))
                .andExpect(jsonPath("$.estado").value("VENDIDO"));
        enviar(patch("/api/v1/vehiculos/" + id + "/estado"), vendido, ADMIN).andExpect(status().isConflict());
        enviar(get("/api/v1/vehiculos").param("estado", "VENDIDO"), null, null).andExpect(jsonPath("$.length()").value(1));

        enviar(delete("/api/v1/categorias/" + idCategoria), null, ADMIN).andExpect(status().isConflict());
        enviar(delete("/api/v1/vehiculos/" + id), null, ADMIN).andExpect(status().isNoContent());
        enviar(delete("/api/v1/categorias/" + idCategoria), null, ADMIN).andExpect(status().isNoContent());
    }
}
