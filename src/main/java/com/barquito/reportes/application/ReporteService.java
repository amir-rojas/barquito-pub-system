package com.barquito.reportes.application;

import com.barquito.reportes.domain.ResumenPeriodoReporte;
import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Puerto de aplicación para la generación de reportes de negocio.
 *
 * <p>Todas las operaciones son de solo lectura. No produce efectos secundarios.
 */
public interface ReporteService {

    /**
     * Retorna el reporte de ventas del día indicado.
     *
     * <p>Si {@code fecha} es {@code null}, se utiliza la fecha actual ({@link LocalDate#now()}).
     *
     * @param fecha fecha a consultar; {@code null} equivale a hoy.
     * @return reporte con totales y desgloses por método de pago.
     */
    VentasDiariasReporte obtenerVentasDiarias(LocalDate fecha);

    /**
     * Retorna el ranking de productos más vendidos en el período indicado.
     *
     * <p>Si {@code limit} supera {@value ReporteServiceImpl#MAX_LIMIT}, se aplica ese máximo.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @param limit cantidad máxima de ítems a retornar (máx. {@value ReporteServiceImpl#MAX_LIMIT}).
     * @return lista ordenada por monto total descendente.
     */
    List<TopProductoItem> obtenerTopProductos(OffsetDateTime desde, OffsetDateTime hasta, int limit);

    /**
     * Retorna las ventas agrupadas por categoría de producto en el período indicado.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return lista ordenada por monto total descendente.
     */
    List<VentasPorCategoriaItem> obtenerVentasPorCategoria(OffsetDateTime desde, OffsetDateTime hasta);

    /**
     * Retorna el resumen financiero consolidado del período indicado.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return resumen con ventas, anulaciones, ingresos, egresos y balance.
     */
    ResumenPeriodoReporte obtenerResumenPeriodo(OffsetDateTime desde, OffsetDateTime hasta);
}
