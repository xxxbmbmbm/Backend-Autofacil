package cl.autofacil.ventas.excepcion;

/** ms-vehiculos no respondió. Se transforma en HTTP 503. */
public class ServicioNoDisponibleExcepcion extends RuntimeException {
    public ServicioNoDisponibleExcepcion() {
        super("El catálogo de vehículos no está disponible en este momento. Intenta nuevamente en unos minutos.");
    }
}
