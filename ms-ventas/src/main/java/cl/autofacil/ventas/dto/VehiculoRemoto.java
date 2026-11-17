package cl.autofacil.ventas.dto;

/** Los campos que ms-ventas necesita de la respuesta de ms-vehiculos. */
public record VehiculoRemoto(Long id, String marca, String modelo, Integer anio, Long precio, String estado) {

    public boolean estaDisponible() {
        return "DISPONIBLE".equals(estado);
    }

    public String descripcion() {
        return marca + " " + modelo + " " + anio;
    }
}
