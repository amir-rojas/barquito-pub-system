package com.barquito.pedidos.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de respuesta para una línea de pedido.
 *
 * @param id             identificador único de la línea.
 * @param pedidoId       id del pedido al que pertenece.
 * @param productoId     id del producto.
 * @param cantidad       cantidad pedida.
 * @param precioUnitario precio unitario snapshot.
 * @param subtotal       subtotal (cantidad * precioUnitario), generado por BD.
 * @param estado         estado actual de la línea.
 * @param notas          notas opcionales.
 * @param creadoEn       timestamp de creación.
 * @param actualizadoEn  timestamp de última actualización.
 */
public record LineaPedidoResponse(
        Long id,
        Long pedidoId,
        Long productoId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        String estado,
        String notas,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn
) {}
