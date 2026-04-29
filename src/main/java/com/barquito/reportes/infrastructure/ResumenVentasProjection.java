package com.barquito.reportes.infrastructure;

import java.math.BigDecimal;

/**
 * Proyección Spring Data para el resumen de ventas de un período.
 *
 * <p>Mapeada automáticamente desde la query nativa en {@link ReporteVentasJpaRepository}.
 */
public interface ResumenVentasProjection {

    /** @return cantidad de ventas pagadas en el período. */
    long getTotalVentas();

    /** @return suma del total de ventas pagadas. */
    BigDecimal getMontoVentas();

    /** @return cantidad de ventas anuladas en el período. */
    long getVentasAnuladas();
}
