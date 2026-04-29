package com.barquito.pedidos.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad de dominio que representa una línea de un pedido.
 *
 * <p>Inmutable por diseño: record sin setters ni estado mutable.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * @param id             identificador único de la línea.
 * @param pedidoId       referencia al pedido al que pertenece.
 * @param productoId     referencia al producto pedido.
 * @param cantidad       cantidad pedida (NUMERIC 12,3).
 * @param precioUnitario precio unitario en el momento del pedido (snapshot).
 * @param subtotal       subtotal GENERATED (cantidad * precioUnitario); puede ser null antes de persistir.
 * @param estado         estado operativo de la línea.
 * @param notas          notas opcionales de la línea.
 * @param creadoEn       fecha y hora de creación.
 * @param actualizadoEn  fecha y hora de última actualización.
 */
public record LineaPedido(
        Long id,
        Long pedidoId,
        Long productoId,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        EstadoLinea estado,
        String notas,
        java.time.OffsetDateTime creadoEn,
        java.time.OffsetDateTime actualizadoEn
) {}
