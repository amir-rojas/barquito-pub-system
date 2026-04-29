package com.barquito.pedidos.application;

import java.math.BigDecimal;

/**
 * Instantánea inmutable de los datos de un producto en el momento de crear una línea.
 *
 * <p>Captura el precio en el momento del pedido para evitar que cambios futuros
 * de precio afecten pedidos ya registrados.
 *
 * @param id      identificador del producto.
 * @param nombre  nombre del producto.
 * @param precio  precio de venta en el momento del snapshot.
 * @param activo  indica si el producto está activo en el catálogo.
 */
public record ProductoSnapshot(
        Long id,
        String nombre,
        BigDecimal precio,
        boolean activo
) {}
