package com.barquito.reportes.infrastructure;

import java.math.BigDecimal;

/**
 * Proyección Spring Data para las ventas agrupadas por categoría.
 *
 * <p>Mapeada automáticamente desde la query nativa en {@link ReporteVentasJpaRepository}.
 */
public interface VentasPorCategoriaProjection {

    /** @return categoría del producto. */
    String getCategoria();

    /** @return suma de unidades vendidas en la categoría. */
    BigDecimal getCantidadVendida();

    /** @return suma del subtotal facturado en la categoría. */
    BigDecimal getMontoTotal();
}
