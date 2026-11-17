package cl.autofacil.vehiculos.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Producto de la tienda (tabla PRODUCTO del modelo propuesto). Cada vehículo es una unidad única. */
@Entity
@Table(name = "vehiculo")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false)
    private Integer anio;

    /** Precio en pesos chilenos (sin decimales). */
    @Column(nullable = false)
    private Long precio;

    /** Precio antes de una rebaja; si existe, el frontend muestra la oferta. */
    @Column(name = "precio_anterior")
    private Long precioAnterior;

    @Column(nullable = false)
    private Integer kilometraje;

    @Column(nullable = false, length = 20)
    private String transmision;

    @Column(nullable = false, length = 500)
    private String imagen;

    @Column(name = "color_fondo", length = 7)
    private String colorFondo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVehiculo estado = EstadoVehiculo.DISPONIBLE;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void antesDeGuardar() {
        fechaCreacion = LocalDateTime.now();
    }
}
