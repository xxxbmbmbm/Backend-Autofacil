package cl.autofacil.usuarios.repositorio;

import cl.autofacil.usuarios.modelo.Rol;
import cl.autofacil.usuarios.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRol(Rol rol);
}
