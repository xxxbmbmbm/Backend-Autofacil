package cl.autofacil.usuarios.modelo;

/** Roles del sistema según el enunciado de la evaluación. */
public enum Rol {
    /** Acceso total al sistema. */
    ADMIN,
    /** Solo ve vehículos y órdenes (boletas). */
    VENDEDOR,
    /** Solo accede a la tienda. */
    CLIENTE
}
