package cl.autofacil.ventas;

import cl.autofacil.ventas.dto.VehiculoRemoto;
import cl.autofacil.ventas.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.ventas.servicio.ClienteVehiculos;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static cl.autofacil.ventas.TokensPrueba.token;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pruebas de integración de la API de boletas; ms-vehiculos se reemplaza por un simulador (mock). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiVentasTest {

    private static final String ADMIN = token(1, "ADMIN");
    private static final String VENDEDOR = token(2, "VENDEDOR");
    private static final String CLIENTE = token(10, "CLIENTE");
    private static final String OTRO_CLIENTE = token(11, "CLIENTE");
    private static final String DIRECCION = "\"direccion\":\"Av. Providencia 1234\",\"comuna\":\"Providencia\",\"ciudad\":\"Santiago\"";

    @Autowired
    private MockMvc api;

    @MockitoBean
    private ClienteVehiculos clienteVehiculos;

    @BeforeEach
    void catalogoSimulado() {
        when(clienteVehiculos.obtener(1L)).thenReturn(new VehiculoRemoto(1L, "Toyota", "Yaris", 2021, 8_990_000L, "DISPONIBLE"));
        when(clienteVehiculos.obtener(2L)).thenReturn(new VehiculoRemoto(2L, "Chevrolet", "Sail", 2020, 7_490_000L, "VENDIDO"));
        when(clienteVehiculos.obtener(3L)).thenReturn(new VehiculoRemoto(3L, "Nissan", "Qashqai", 2022, 14_990_000L, "DISPONIBLE"));
    }

    private ResultActions enviar(MockHttpServletRequestBuilder peticion, String json, String token) throws Exception {
        if (json != null) peticion.contentType(MediaType.APPLICATION_JSON).content(json);
        if (token != null) peticion.header("Authorization", "Bearer " + token);
        return api.perform(peticion);
    }

    private static String compra(String ids) {
        return "{\"vehiculoIds\":" + ids + "," + DIRECCION + "}";
    }

    @Test
    void clienteCompraYElAdministradorGestionaLaBoleta() throws Exception {
        enviar(post("/api/v1/boletas"), compra("[1]"), null).andExpect(status().isUnauthorized());
        enviar(post("/api/v1/boletas"), compra("[1]"), VENDEDOR).andExpect(status().isForbidden());
        enviar(post("/api/v1/boletas"), "{\"vehiculoIds\":[]}", CLIENTE)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.vehiculoIds").exists());
        enviar(post("/api/v1/boletas"), compra("[1,1]"), CLIENTE).andExpect(status().isConflict());
        enviar(post("/api/v1/boletas"), compra("[2]"), CLIENTE)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El Chevrolet Sail 2020 ya no está disponible."));

        String creada = enviar(post("/api/v1/boletas"), compra("[1,3]"), CLIENTE)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(23_980_000)) // calculado en el servidor
                .andExpect(jsonPath("$.estado").value("PAGADA"))
                .andExpect(jsonPath("$.detalles.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(creada, "$.id");
        verify(clienteVehiculos).cambiarEstado(1L, "VENDIDO");
        verify(clienteVehiculos).cambiarEstado(3L, "VENDIDO");

        // Quién puede ver qué
        enviar(get("/api/v1/boletas/mias"), null, CLIENTE).andExpect(jsonPath("$[0].id").value(id));
        enviar(get("/api/v1/boletas"), null, CLIENTE).andExpect(status().isForbidden());
        enviar(get("/api/v1/boletas").param("estado", "PAGADA"), null, VENDEDOR).andExpect(status().isOk());
        enviar(get("/api/v1/boletas/" + id), null, VENDEDOR).andExpect(jsonPath("$.nombreCliente").value("Usuario CLIENTE"));
        enviar(get("/api/v1/boletas/" + id), null, CLIENTE).andExpect(status().isOk());
        enviar(get("/api/v1/boletas/" + id), null, OTRO_CLIENTE).andExpect(status().isForbidden());
        enviar(get("/api/v1/boletas/999"), null, ADMIN).andExpect(status().isNotFound());
        enviar(patch("/api/v1/boletas/" + id + "/estado"), "{\"estado\":\"ENTREGADA\"}", VENDEDOR).andExpect(status().isForbidden());

        // Ciclo de estados
        String url = "/api/v1/boletas/" + id + "/estado";
        enviar(patch(url), "{\"estado\":\"ENTREGADA\"}", ADMIN).andExpect(jsonPath("$.estado").value("ENTREGADA"));
        enviar(patch(url), "{\"estado\":\"PAGADA\"}", ADMIN).andExpect(status().isConflict());
        enviar(delete("/api/v1/boletas/" + id), null, ADMIN).andExpect(status().isConflict());
        enviar(patch(url), "{\"estado\":\"ANULADA\"}", ADMIN).andExpect(jsonPath("$.estado").value("ANULADA"));
        verify(clienteVehiculos).cambiarEstado(1L, "DISPONIBLE"); // vuelve a la tienda
        enviar(patch(url), "{\"estado\":\"ANULADA\"}", ADMIN).andExpect(status().isOk());
        enviar(patch(url), "{\"estado\":\"ENTREGADA\"}", ADMIN).andExpect(status().isConflict());
        enviar(delete("/api/v1/boletas/" + id), null, ADMIN).andExpect(status().isNoContent());
    }

    @Test
    void siUnVehiculoNoSePuedeMarcarSeRevierteLaCompra() throws Exception {
        doThrow(new ReglaNegocioExcepcion("Uno de los vehículos ya fue vendido."))
                .when(clienteVehiculos).cambiarEstado(3L, "VENDIDO");
        int antes = JsonPath.<Integer>read(enviar(get("/api/v1/boletas"), null, ADMIN)
                .andReturn().getResponse().getContentAsString(), "$.length()");

        enviar(post("/api/v1/boletas"), compra("[1,3]"), CLIENTE).andExpect(status().isConflict());

        verify(clienteVehiculos).cambiarEstado(1L, "DISPONIBLE"); // se devuelve el que alcanzó a marcarse
        enviar(get("/api/v1/boletas"), null, ADMIN).andExpect(jsonPath("$.length()").value(antes)); // sin boleta
    }
}
