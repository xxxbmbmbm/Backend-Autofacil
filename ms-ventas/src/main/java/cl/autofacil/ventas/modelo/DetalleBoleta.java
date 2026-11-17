package cl.autofacil.ventas.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Línea de la boleta (tabla DETALLE_BOLETA). Guarda una copia del precio y la descripción
 * del vehículo al momento de la compra, para que la boleta no cambie si después se edita el vehículo.
 */
@Entity
@Table(name = "detalle_boleta")
@Getter
@Setter
@NoArgsConstructor
public class DetalleBoleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "boleta_id")
    private Boleta boleta;

    /** Id del vehículo en ms-vehiculos (tabla PRODUCTO). */
    @Column(name = "vehiculo_id", nullable = false)
    private Long vehiculoId;

    @Column(nullable = false, length = 150)
    private String descripcion;

    @Column(name = "precio_unitario", nullable = false)
    private Long precioUnitario;

    /** Siempre 1: cada vehículo es una unidad única. */
    @Column(nullable = false)
    private Integer cantidad = 1;

    @Column(nullable = false)
    private Long subtotal;
}
