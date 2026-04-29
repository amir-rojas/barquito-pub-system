package com.barquito.caja.api;

import java.math.BigDecimal;

/**
 * Respuesta con los datos de un detalle de venta.
 *
 * @param id              identificador del detalle.
 * @param productoId      referencia al producto original.
 * @param productoNombre  nombre del producto en el momento de la facturación (snapshot).
 * @param cantidad        cantidad facturada.
 * @param precioUnitario  precio unitario en el momento de la facturación (snapshot).
 * @param subtotal        subtotal calculado por la base de datos.
 */
public record DetalleVentaResponse(
        Long id,
        Long productoId,
        String productoNombre,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
