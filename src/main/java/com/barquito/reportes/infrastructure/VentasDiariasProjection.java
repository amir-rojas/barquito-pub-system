package com.barquito.reportes.infrastructure;

import java.math.BigDecimal;

/**
 * Proyección Spring Data para el reporte de ventas diarias.
 *
 * <p>Mapeada automáticamente desde la query nativa en {@link ReporteVentasJpaRepository}.
 */
public interface VentasDiariasProjection {

    /** @return cantidad de ventas pagadas en el día. */
    long getTotalVentas();

    /** @return suma total de las ventas pagadas. */
    BigDecimal getMontoTotal();

    /** @return suma de ventas cobradas en efectivo. */
    BigDecimal getMontoEfectivo();

    /** @return suma de ventas cobradas con QR. */
    BigDecimal getMontoQr();
}
