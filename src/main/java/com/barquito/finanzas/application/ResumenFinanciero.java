package com.barquito.finanzas.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Resumen financiero para un período dado.
 *
 * @param totalIngresos suma de todos los ingresos en el período.
 * @param totalEgresos  suma de todos los egresos en el período.
 * @param balance       diferencia entre ingresos y egresos ({@code totalIngresos - totalEgresos}).
 * @param desde         inicio del período consultado.
 * @param hasta         fin del período consultado.
 */
public record ResumenFinanciero(
        BigDecimal totalIngresos,
        BigDecimal totalEgresos,
        BigDecimal balance,
        OffsetDateTime desde,
        OffsetDateTime hasta
) {}
