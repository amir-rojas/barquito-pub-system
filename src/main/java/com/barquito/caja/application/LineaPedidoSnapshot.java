package com.barquito.caja.application;

import java.math.BigDecimal;

/**
 * Snapshot de una línea de pedido leída por el bounded context de caja.
 *
 * <p>Valor inmutable capturado al momento de crear la venta. Incluye
 * {@code productoNombre} y {@code precioUnitario} para construir los
 * {@code DetalleVenta} con snapshot inmutable.
 *
 * @param lineaPedidoId   identificador de la línea en el contexto pedidos.
 * @param productoId      referencia al producto original.
 * @param productoNombre  nombre del producto al momento de la lectura.
 * @param cantidad        cantidad pedida.
 * @param precioUnitario  precio unitario al momento de la lectura.
 * @param subtotal        subtotal de la línea (cantidad × precioUnitario).
 * @param estado          estado de la línea al momento de la lectura.
 */
public record LineaPedidoSnapshot(
        Long lineaPedidoId,
        Long productoId,
        String productoNombre,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        EstadoLineaSnapshot estado
) {}
