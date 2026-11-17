package cl.autofacil.contacto.seguridad;

/** Datos del usuario que vienen dentro del JWT; quedan disponibles en cada petición autenticada. */
public record UsuarioAutenticado(Long id, String correo, String nombre, String rol) {
}
