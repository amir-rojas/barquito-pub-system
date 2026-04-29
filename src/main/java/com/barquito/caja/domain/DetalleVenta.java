package com.barquito.caja.domain;

import java.math.BigDecimal;

/**
 * Snapshot inmutable de una línea de pedido en el momento de la facturación.
 *
 * <p>Captura {@code productoNombre} y {@code precioUnitario} al momento de crear la venta.
 * Cambios posteriores al producto no afectan este registro (inmutabilidad de auditoría).
 *
 * <p>{@code subtotal} es calculado por la base de datos como
 * {@code GENERATED ALWAYS AS (cantidad * precio_unitario) STORED}.
 * El código de aplicación nunca lo asigna; puede ser {@code null} antes de la persistencia.
 *
 * @param id              identificador único del detalle.
 * @param ventaId         referencia a la venta que contiene este detalle.
 * @param productoId      referencia al producto original (auditoría).
 * @param productoNombre  nombre del producto al momento de facturar (snapshot).
 * @param cantidad        cantidad facturada.
 * @param precioUnitario  precio unitario al momento de facturar (snapshot).
 * @param subtotal        subtotal calculado por la base de datos; {@code null} antes de persistir.
 */
public record DetalleVenta(
        Long id,
        Long ventaId,
        Long productoId,
        String productoNombre,
        BigDecimal cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {}
