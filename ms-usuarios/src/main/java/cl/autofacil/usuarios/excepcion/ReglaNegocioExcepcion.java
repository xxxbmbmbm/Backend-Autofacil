package cl.autofacil.usuarios.excepcion;

/** Una regla de negocio no se cumple (por ejemplo, correo repetido). Se transforma en HTTP 409. */
public class ReglaNegocioExcepcion extends RuntimeException {
    public ReglaNegocioExcepcion(String mensaje) {
        super(mensaje);
    }
}
