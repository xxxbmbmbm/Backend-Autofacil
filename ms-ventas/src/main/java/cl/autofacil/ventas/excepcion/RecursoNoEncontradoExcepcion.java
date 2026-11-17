package cl.autofacil.ventas.excepcion;

/** Se transforma en una respuesta HTTP 404. */
public class RecursoNoEncontradoExcepcion extends RuntimeException {
    public RecursoNoEncontradoExcepcion(String mensaje) {
        super(mensaje);
    }
}
