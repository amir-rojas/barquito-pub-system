package com.barquito.reportes.domain;

import java.math.BigDecimal;

/**
 * Ítem de ranking de productos más vendidos en un período.
 *
 * @param productoId      identificador del producto.
 * @param nombre          nombre del producto al momento de la venta (snapshot).
 * @param categoria       categoría del producto.
 * @param cantidadVendida suma de unidades vendidas.
 * @param montoTotal      suma del subtotal facturado.
 */
public record TopProductoItem(
        Long productoId,
        String nombre,
        String categoria,
        BigDecimal cantidadVendida,
        BigDecimal montoTotal
) {}
