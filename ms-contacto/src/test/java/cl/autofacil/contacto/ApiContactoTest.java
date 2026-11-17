package cl.autofacil.contacto;

import cl.autofacil.contacto.servicio.EnlacesContacto;
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

import static cl.autofacil.contacto.TokensPrueba.token;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContactoTest {

    private static final String ADMIN = token(1, "ADMIN");
    private static final String CLIENTE = token(10, "CLIENTE");

    @Autowired
    private MockMvc api;

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String json, String token) throws Exception {
        if (json != null) peticion.contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) peticion.header("Authorization", "Bearer " + token);
        return api.perform(peticion);
    }

    @Test
    void clienteEscribeYElAdministradorLoAtiendeDesdeLaBandeja() throws Exception {
        String mensaje = """
                {"nombre":"Ana Pérez","correo":"Ana@Correo.cl","telefono":"+56 9 1234 5678",
                 "asunto":"Consulta por un vehículo","mensaje":"¿El Yaris sigue disponible?","vehiculoId":1}""";
        enviar(post("/api/v1/contactos"), mensaje, null).andExpect(status().isUnauthorized());
        enviar(post("/api/v1/contactos"), "{\"asunto\":\"Spam\",\"mensaje\":\"corto\"}", CLIENTE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.asunto").value("Selecciona un asunto de la lista."));

        long nuevasAntes = ((Number) JsonPath.read(enviar(get("/api/v1/contactos/resumen"), null, ADMIN)
                .andReturn().getResponse().getContentAsString(), "$.nuevas")).longValue();
        Integer id = JsonPath.read(enviar(post("/api/v1/contactos"), mensaje, CLIENTE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("NUEVA"))
                .andExpect(jsonPath("$.enlaces").doesNotExist()) // el cliente no ve datos internos
                .andReturn().getResponse().getContentAsString(), "$.id");

        // Notificación nueva en la bandeja, con enlaces listos para responder
        enviar(get("/api/v1/contactos/resumen"), null, ADMIN).andExpect(jsonPath("$.nuevas").value(nuevasAntes + 1));
        enviar(get("/api/v1/contactos").param("estado", "NUEVA"), null, ADMIN).andExpect(status().isOk());
        enviar(get("/api/v1/contactos/" + id), null, ADMIN)
                .andExpect(jsonPath("$.correo").value("ana@correo.cl"))
                .andExpect(jsonPath("$.enlaces.whatsapp").value(org.hamcrest.Matchers.startsWith("https://wa.me/56912345678?text=Hola%20Ana")))
                .andExpect(jsonPath("$.enlaces.llamada").value("tel:+56912345678"))
                .andExpect(jsonPath("$.enlaces.correo").value(org.hamcrest.Matchers.startsWith("mailto:ana@correo.cl?subject=")));

        // Solo el administrador ve la bandeja
        enviar(get("/api/v1/contactos"), null, CLIENTE).andExpect(status().isForbidden());
        enviar(get("/api/v1/contactos"), null, token(2, "VENDEDOR")).andExpect(status().isForbidden());

        // Gestión: no se puede marcar RESPONDIDA sin canal, ni volver a NUEVA
        String url = "/api/v1/contactos/" + id;
        enviar(patch(url), "{\"estado\":\"RESPONDIDA\"}", ADMIN).andExpect(status().isConflict());
        enviar(patch(url), "{\"estado\":\"EN_PROCESO\",\"notasInternas\":\"Llamar en la tarde\"}", ADMIN)
                .andExpect(jsonPath("$.notasInternas").value("Llamar en la tarde"));
        enviar(patch(url), "{\"estado\":\"NUEVA\"}", ADMIN).andExpect(status().isConflict());
        enviar(patch(url), "{\"estado\":\"RESPONDIDA\",\"canalRespuesta\":\"WHATSAPP\"}", ADMIN)
                .andExpect(jsonPath("$.canalRespuesta").value("WHATSAPP"));

        // El cliente ve el avance, pero no las notas internas
        enviar(get("/api/v1/contactos/mias"), null, CLIENTE)
                .andExpect(jsonPath("$[0].estado").value("RESPONDIDA"))
                .andExpect(jsonPath("$[0].notasInternas").doesNotExist());

        enviar(delete(url), null, ADMIN).andExpect(status().isNoContent());
        enviar(get(url), null, ADMIN).andExpect(status().isNotFound());
    }

    @Test
    void normalizaTelefonosChilenos() {
        assertThat(EnlacesContacto.normalizarTelefono("912345678")).isEqualTo("56912345678");
        assertThat(EnlacesContacto.normalizarTelefono("1234 5678")).isEqualTo("56912345678");
        assertThat(EnlacesContacto.normalizarTelefono("123")).isNull();
        assertThat(EnlacesContacto.whatsapp(null, "Ana", "Otro")).isNull();
        assertThat(EnlacesContacto.llamada("123")).isNull();
    }
}
