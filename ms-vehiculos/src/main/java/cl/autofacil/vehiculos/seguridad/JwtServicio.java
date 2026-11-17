package cl.autofacil.vehiculos.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Optional;

/**
 * Verifica los tokens emitidos por ms-usuarios. Este servicio no genera tokens de usuario:
 * solo comprueba la firma con el secreto compartido, sin llamar a ms-usuarios en cada petición.
 */
@Service
public class JwtServicio {

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
}
