package cl.autofacil.vehiculos.dto;

import cl.autofacil.vehiculos.modelo.Categoria;

public record CategoriaRespuesta(Long id, String nombre, String descripcion) {
    public static CategoriaRespuesta desde(Categoria categoria) {
        return new CategoriaRespuesta(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }
}
