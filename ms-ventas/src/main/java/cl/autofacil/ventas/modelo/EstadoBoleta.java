package cl.autofacil.ventas.modelo;

public enum EstadoBoleta {
    /** Compra confirmada; los vehículos quedan VENDIDOS. */
    PAGADA,
    /** El vehículo ya se entregó al cliente. */
    ENTREGADA,
    /** Compra anulada; los vehículos vuelven a estar DISPONIBLES. */
    ANULADA
}
