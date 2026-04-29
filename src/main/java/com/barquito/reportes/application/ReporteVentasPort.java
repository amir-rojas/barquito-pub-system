package com.barquito.reportes.application;

import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;
import com.barquito.reportes.domain.ResumenVentasData;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Puerto de salida para consultas de reportes de ventas.
 *
 * <p>Aísla la capa de aplicación de la implementación JPA concreta.
 */
public interface ReporteVentasPort {

    /**
     * Retorna totales de ventas pagadas para un día específico.
     *
     * @param fecha fecha a consultar.
     * @return datos de ventas diarias.
     */
    VentasDiariasReporte obtenerVentasDiarias(LocalDate fecha);

    /**
     * Retorna el ranking de productos más vendidos en el período.
     *
     * @param desde inicio del período.
     * @param hasta fin del período.
     * @param limit cantidad máxima de resultados (1-50).
     * @return lista ordenada por monto total descendente.
     */
    List<TopProductoItem> obtenerTopProductos(OffsetDateTime desde, OffsetDateTime hasta, int limit);

    /**
     * Retorna ventas agrupadas por categoría en el período.
     *
     * @param desde inicio del período.
     * @param hasta fin del período.
     * @return lista ordenada por monto total descendente.
     */
    List<VentasPorCategoriaItem> obtenerVentasPorCategoria(OffsetDateTime desde, OffsetDateTime hasta);

    /**
     * Retorna el resumen de ventas (pagadas y anuladas) en el período.
     *
     * @param desde inicio del período.
     * @param hasta fin del período.
     * @return datos de resumen de ventas.
     */
    ResumenVentasData obtenerResumenVentas(OffsetDateTime desde, OffsetDateTime hasta);
}
