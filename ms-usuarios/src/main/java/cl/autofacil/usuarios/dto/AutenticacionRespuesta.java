package cl.autofacil.usuarios.dto;

public record AutenticacionRespuesta(
        String tokenAcceso,
        String tokenRenovacion,
        String tipoToken,
        long expiraEnSegundos,
        UsuarioRespuesta usuario
) {
}
