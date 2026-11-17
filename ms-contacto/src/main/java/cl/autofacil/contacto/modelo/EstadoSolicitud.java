package cl.autofacil.contacto.modelo;

public enum EstadoSolicitud {
    /** Recién llegada: cuenta como notificación sin leer en la bandeja del administrador. */
    NUEVA,
    /** El administrador ya la vio y está gestionándola. */
    EN_PROCESO,
    /** Ya se contactó al cliente. */
    RESPONDIDA,
    /** Caso terminado. */
    CERRADA
}
