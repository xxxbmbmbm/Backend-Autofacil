package cl.autofacil.ventas.dto;

import cl.autofacil.ventas.modelo.Boleta;
import cl.autofacil.ventas.modelo.DetalleBoleta;
import cl.autofacil.ventas.modelo.EstadoBoleta;

import java.time.LocalDateTime;
import java.util.List;

public record BoletaRespuesta(
        Long id,
        Long usuarioId,
        String nombreCliente,
        String correoCliente,
        String direccion,
        String comuna,
        String ciudad,
        Long total,
        EstadoBoleta estado,
        LocalDateTime fecha,
        List<Detalle> detalles
) {
    public record Detalle(Long id, Long vehiculoId, String descripcion, Long precioUnitario, Integer cantidad, Long subtotal) {
        static Detalle desde(DetalleBoleta d) {
            return new Detalle(d.getId(), d.getVehiculoId(), d.getDescripcion(), d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal());
        }
    }

    public static BoletaRespuesta desde(Boleta b) {
        return new BoletaRespuesta(
                b.getId(), b.getUsuarioId(), b.getNombreCliente(), b.getCorreoCliente(),
                b.getDireccion(), b.getComuna(), b.getCiudad(), b.getTotal(), b.getEstado(), b.getFecha(),
                b.getDetalles().stream().map(Detalle::desde).toList());
    }
}
