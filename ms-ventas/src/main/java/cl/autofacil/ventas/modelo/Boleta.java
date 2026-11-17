package cl.autofacil.ventas.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de compra (tabla BOLETA). El usuario vive en ms-usuarios, por eso aquí solo se guarda su id
 * y una copia de su nombre y correo (la línea punteada USUARIO - BOLETA del modelo propuesto).
 */
@Entity
@Table(name = "boleta")
@Getter
@Setter
@NoArgsConstructor
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nombre_cliente", nullable = false, length = 100)
    private String nombreCliente;

    @Column(name = "correo_cliente", nullable = false, length = 120)
    private String correoCliente;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 80)
    private String comuna;

    @Column(nullable = false, length = 80)
    private String ciudad;

    /** Total en pesos chilenos, calculado en el servidor con los precios reales. */
    @Column(nullable = false)
    private Long total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBoleta estado = EstadoBoleta.PAGADA;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @OneToMany(mappedBy = "boleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleBoleta> detalles = new ArrayList<>();

    @PrePersist
    void antesDeGuardar() {
        fecha = LocalDateTime.now();
    }

    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.setBoleta(this);
        detalles.add(detalle);
    }
}
