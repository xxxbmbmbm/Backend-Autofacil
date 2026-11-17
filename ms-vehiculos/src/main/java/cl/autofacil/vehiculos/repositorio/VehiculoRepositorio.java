package cl.autofacil.vehiculos.repositorio;

import cl.autofacil.vehiculos.modelo.EstadoVehiculo;
import cl.autofacil.vehiculos.modelo.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VehiculoRepositorio extends JpaRepository<Vehiculo, Long> {

    /** Cada filtro es opcional: si llega null, no se aplica. Los más nuevos primero. */
    @Query("""
            select v from Vehiculo v
            where (:marca is null or lower(v.marca) = lower(:marca))
              and (:categoriaId is null or v.categoria.id = :categoriaId)
              and (:estado is null or v.estado = :estado)
            order by v.id desc""")
    List<Vehiculo> buscar(String marca, Long categoriaId, EstadoVehiculo estado);

    @Query("select distinct v.marca from Vehiculo v order by v.marca")
    List<String> listarMarcas();

    boolean existsByCategoriaId(Long categoriaId);
}
