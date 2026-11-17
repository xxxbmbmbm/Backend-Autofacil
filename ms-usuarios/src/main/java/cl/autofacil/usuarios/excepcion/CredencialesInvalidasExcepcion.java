package cl.autofacil.usuarios.excepcion;

/** Correo/contraseña o token de renovación inválidos. Se transforma en HTTP 401. */
public class CredencialesInvalidasExcepcion extends RuntimeException {
    public CredencialesInvalidasExcepcion(String mensaje) {
        super(mensaje);
    }
}
