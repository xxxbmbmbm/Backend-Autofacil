package cl.autofacil.usuarios.repositorio;

import cl.autofacil.usuarios.modelo.TokenRenovacion;
import cl.autofacil.usuarios.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRenovacionRepositorio extends JpaRepository<TokenRenovacion, Long> {
    /** Busca por el hash SHA-256 del token. */
    Optional<TokenRenovacion> findByToken(String token);

    @Modifying
    @Query("update TokenRenovacion t set t.revocado = true where t.usuario = :usuario and t.revocado = false")
    void revocarTodosDelUsuario(Usuario usuario);

    @Modifying
    @Query("delete from TokenRenovacion t where t.usuario = :usuario")
    void eliminarDelUsuario(Usuario usuario);
}
