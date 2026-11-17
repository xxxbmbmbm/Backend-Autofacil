package cl.autofacil.ventas.servicio;

import cl.autofacil.ventas.dto.VehiculoRemoto;
import cl.autofacil.ventas.excepcion.RecursoNoEncontradoExcepcion;
import cl.autofacil.ventas.excepcion.ReglaNegocioExcepcion;
import cl.autofacil.ventas.excepcion.ServicioNoDisponibleExcepcion;
import cl.autofacil.ventas.seguridad.JwtServicio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/** Comunicación REST con ms-vehiculos (microservicio a microservicio). */
@Component
public class ClienteVehiculos {

    private final RestClient cliente;
    private final JwtServicio jwtServicio;

    public ClienteVehiculos(@Value("${autofacil.servicios.vehiculos-url}") String urlVehiculos, JwtServicio jwtServicio) {
        this.cliente = RestClient.builder().baseUrl(urlVehiculos).build();
        this.jwtServicio = jwtServicio;
    }

    public VehiculoRemoto obtener(Long id) {
        try {
            return cliente.get().uri("/api/v1/vehiculos/{id}", id).retrieve().body(VehiculoRemoto.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoExcepcion("No existe el vehículo con id " + id + ".");
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleExcepcion();
        }
    }

    /** estado: "VENDIDO" al comprar o "DISPONIBLE" al anular. Usa un token con rol SERVICIO. */
    public void cambiarEstado(Long id, String estado) {
        try {
            cliente.patch().uri("/api/v1/vehiculos/{id}/estado", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtServicio.generarTokenServicio())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("estado", estado))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
            throw new ReglaNegocioExcepcion("Uno de los vehículos ya fue vendido. Actualiza la página e intenta de nuevo.");
        } catch (RestClientException e) {
            throw new ServicioNoDisponibleExcepcion();
        }
    }
}
