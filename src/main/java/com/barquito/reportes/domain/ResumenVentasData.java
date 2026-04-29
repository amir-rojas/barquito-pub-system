package com.barquito.reportes.domain;

/**
 * Datos de resumen de ventas para el período consultado.
 *
 * @param totalVentas    cantidad de ventas PAGADAS.
 * @param montoVentas    monto total de ventas PAGADAS.
 * @param ventasAnuladas cantidad de ventas ANULADAS.
 */
public record ResumenVentasData(long totalVentas, java.math.BigDecimal montoVentas, long ventasAnuladas) {}
