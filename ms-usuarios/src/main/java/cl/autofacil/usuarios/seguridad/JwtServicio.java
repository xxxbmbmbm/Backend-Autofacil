package cl.autofacil.usuarios.seguridad;

import cl.autofacil.usuarios.modelo.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/**
 * Genera y verifica los tokens de acceso (JWT firmados con HS256).
 * Los demás microservicios usan la misma clave secreta para verificarlos sin llamar a este servicio.
 */
@Service
public class JwtServicio {

    private final SecretKey claveFirma;
    private final Duration duracionTokenAcceso;
    private final String emisor;

    public JwtServicio(
            @Value("${autofacil.jwt.secreto}") String secretoBase64,
            @Value("${autofacil.jwt.minutos-acceso}") long minutosAcceso,
            @Value("${autofacil.jwt.emisor}") String emisor) {
        this.claveFirma = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.duracionTokenAcceso = Duration.ofMinutes(minutosAcceso);
        this.emisor = emisor;
    }

    public String generarTokenAcceso(Usuario usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(emisor)
                .subject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracionTokenAcceso)))
                .signWith(claveFirma)
                .compact();
    }

    /** Devuelve el usuario si el token tiene firma válida, emisor correcto y no ha expirado. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims datos = Jwts.parser()
                    .verifyWith(claveFirma)
                    .requireIssuer(emisor)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UsuarioAutenticado(
                    datos.get("id", Long.class),
                    datos.getSubject(),
                    datos.get("nombre", String.class),
                    datos.get("rol", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long segundosDeVigencia() {
        return duracionTokenAcceso.toSeconds();
    }
}
