package cl.autofacil.contacto.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Mensaje enviado desde el formulario de contacto. */
@Entity
@Table(name = "solicitud_contacto")
@Getter
@Setter
@NoArgsConstructor
public class SolicitudContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id del usuario en ms-usuarios (quien envió el formulario con su sesión iniciada). */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String correo;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, length = 50)
    private String asunto;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    /** Vehículo por el que consulta (opcional). */
    @Column(name = "vehiculo_id")
    private Long vehiculoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.NUEVA;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_respuesta", length = 20)
    private CanalContacto canalRespuesta;

    /** Notas privadas del administrador; el cliente nunca las ve. */
    @Column(name = "notas_internas", length = 1000)
    private String notasInternas;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    void antesDeGuardar() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = fechaCreacion;
    }

    @PreUpdate
    void antesDeActualizar() {
        fechaActualizacion = LocalDateTime.now();
    }
}
