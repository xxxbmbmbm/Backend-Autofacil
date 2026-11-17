package cl.autofacil.ventas.seguridad;

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
 * Verifica los tokens de los usuarios (emitidos por ms-usuarios) y genera un token propio de corta
 * duración con rol SERVICIO para llamar a ms-vehiculos al confirmar o anular una compra.
 */
@Service
public class JwtServicio {

    private static final Duration DURACION_TOKEN_SERVICIO = Duration.ofMinutes(1);

    private final SecretKey claveFirma;
    private final String emisor;

    public JwtServicio(@Value("${autofacil.jwt.secreto}") String secretoBase64,
                       @Value("${autofacil.jwt.emisor}") String emisor) {
        this.claveFirma = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.emisor = emisor;
    }

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

    public String generarTokenServicio() {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .issuer(emisor)
                .subject("ms-ventas")
                .claim("id", 0L)
                .claim("nombre", "ms-ventas")
                .claim("rol", "SERVICIO")
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(DURACION_TOKEN_SERVICIO)))
                .signWith(claveFirma)
                .compact();
    }
}
