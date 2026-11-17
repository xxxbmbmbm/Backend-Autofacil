package cl.autofacil.vehiculos.repositorio;

import cl.autofacil.vehiculos.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
}
