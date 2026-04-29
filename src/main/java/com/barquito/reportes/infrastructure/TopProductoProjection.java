package com.barquito.reportes.infrastructure;

import java.math.BigDecimal;

/**
 * Proyección Spring Data para el ranking de top productos.
 *
 * <p>Mapeada automáticamente desde la query nativa en {@link ReporteVentasJpaRepository}.
 */
public interface TopProductoProjection {

    /** @return identificador del producto (puede ser null si el producto fue eliminado). */
    Long getProductoId();

    /** @return nombre snapshot del producto al momento de la venta. */
    String getNombre();

    /** @return categoría del producto. */
    String getCategoria();

    /** @return suma de unidades vendidas. */
    BigDecimal getCantidadVendida();

    /** @return suma del subtotal facturado. */
    BigDecimal getMontoTotal();
}
