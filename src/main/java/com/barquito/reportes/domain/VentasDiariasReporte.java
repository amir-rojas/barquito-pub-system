package com.barquito.reportes.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reporte agregado de ventas para un día específico.
 *
 * <p>Solo incluye ventas en estado {@code PAGADA}.
 *
 * @param fecha        fecha del reporte.
 * @param totalVentas  cantidad de ventas pagadas en el día.
 * @param montoTotal   suma total de ventas pagadas.
 * @param montoEfectivo suma de ventas cobradas en efectivo.
 * @param montoQr      suma de ventas cobradas con QR.
 */
public record VentasDiariasReporte(
        LocalDate fecha,
        int totalVentas,
        BigDecimal montoTotal,
        BigDecimal montoEfectivo,
        BigDecimal montoQr
) {}
