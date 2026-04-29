package com.barquito.reportes.domain;

import java.math.BigDecimal;

/**
 * Ítem de ventas agrupadas por categoría de producto en un período.
 *
 * @param categoria       categoría del producto.
 * @param cantidadVendida suma de unidades vendidas en la categoría.
 * @param montoTotal      suma del subtotal facturado en la categoría.
 */
public record VentasPorCategoriaItem(
        String categoria,
        BigDecimal cantidadVendida,
        BigDecimal montoTotal
) {}
