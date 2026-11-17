package cl.autofacil.usuarios.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Token de renovación (refresh token). Se guarda en la base de datos para poder
 * revocarlo al cerrar sesión o al rotarlo, a diferencia del token de acceso que es stateless.
 */
@Entity
@Table(name = "token_renovacion")
@Getter
@Setter
@NoArgsConstructor
public class TokenRenovacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    @Column(nullable = false)
    private boolean revocado = false;

    public boolean estaVigente() {
        return !revocado && expiraEn.isAfter(Instant.now());
    }
}
