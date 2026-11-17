package cl.autofacil.vehiculos.dto;

import cl.autofacil.vehiculos.modelo.EstadoVehiculo;
import cl.autofacil.vehiculos.modelo.Vehiculo;

/** "tipo" repite el nombre de la categoría para que el frontend lo use igual que antes. */
public record VehiculoRespuesta(
        Long id,
        String marca,
        String modelo,
        Integer anio,
        Long precio,
        Long precioAnterior,
        Integer kilometraje,
        String transmision,
        String imagen,
        String colorFondo,
        EstadoVehiculo estado,
        Long categoriaId,
        String tipo
) {
    public static VehiculoRespuesta desde(Vehiculo v) {
        return new VehiculoRespuesta(
                v.getId(), v.getMarca(), v.getModelo(), v.getAnio(), v.getPrecio(), v.getPrecioAnterior(),
                v.getKilometraje(), v.getTransmision(), v.getImagen(), v.getColorFondo(), v.getEstado(),
                v.getCategoria().getId(), v.getCategoria().getNombre());
    }
}
