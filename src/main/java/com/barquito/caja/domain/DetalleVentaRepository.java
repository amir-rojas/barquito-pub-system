package com.barquito.caja.domain;

import java.util.List;

/**
 * Puerto de salida para la persistencia de {@link DetalleVenta}.
 *
 * <p>Implementado en la capa de infraestructura.
 */
public interface DetalleVentaRepository {

    /**
     * Persiste todos los detalles de una venta en batch.
     *
     * <p>Tras la persistencia, el campo {@code subtotal} de cada detalle será
     * poblado por la base de datos (columna GENERATED ALWAYS AS STORED).
     *
     * @param detalles lista de detalles a persistir.
     * @return lista de detalles persistidos con todos los campos populados.
     */
    List<DetalleVenta> saveAll(List<DetalleVenta> detalles);

    /**
     * Retorna todos los detalles asociados a una venta.
     *
     * @param ventaId el identificador de la venta.
     * @return lista de detalles; vacía si la venta no tiene detalles.
     */
    List<DetalleVenta> findByVentaId(Long ventaId);
}
