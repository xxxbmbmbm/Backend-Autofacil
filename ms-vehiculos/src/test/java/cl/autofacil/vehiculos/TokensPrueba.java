package cl.autofacil.vehiculos;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

/** Firma tokens igual que ms-usuarios (mismo secreto por defecto), para probar cada rol sin levantar ese servicio. */
public final class TokensPrueba {

    private static final String SECRETO = "Q2FtYmlhRXN0ZVNlY3JldG9FblByb2R1Y2Npb25BdXRvRmFjaWwyMDI2IQ==";

    private TokensPrueba() {
    }

    public static String token(long id, String rol) {
        return Jwts.builder()
                .issuer("autofacil")
                .subject(rol.toLowerCase() + "@autofacil.cl")
                .claim("id", id)
                .claim("nombre", "Usuario " + rol)
                .claim("rol", rol)
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO)))
                .compact();
    }
}
