package com.barquito.reportes.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Resumen financiero consolidado de un período de tiempo.
 *
 * <p>Incluye conteos y montos de ventas, junto con el balance
 * de transacciones financieras (ingresos - egresos).
 *
 * @param desde          inicio del período (inclusive).
 * @param hasta          fin del período (inclusive).
 * @param totalVentas    cantidad de ventas en estado {@code PAGADA}.
 * @param montoVentas    suma del total de ventas pagadas.
 * @param ventasAnuladas cantidad de ventas en estado {@code ANULADA}.
 * @param totalIngresos  suma de transacciones de tipo {@code ingreso}.
 * @param totalEgresos   suma de transacciones de tipo {@code egreso}.
 * @param balance        diferencia {@code totalIngresos - totalEgresos}.
 */
public record ResumenPeriodoReporte(
        OffsetDateTime desde,
        OffsetDateTime hasta,
        int totalVentas,
        BigDecimal montoVentas,
        int ventasAnuladas,
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal balance
) {}
