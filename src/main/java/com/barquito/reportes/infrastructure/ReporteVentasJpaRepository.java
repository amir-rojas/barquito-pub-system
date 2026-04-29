package com.barquito.reportes.infrastructure;

import com.barquito.caja.infrastructure.VentaEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repositorio JPA de solo lectura para reportes de ventas.
 *
 * <p>Reutiliza {@link VentaEntity} (tabla {@code ventas}) sin modificar la entidad.
 * Todas las queries son nativas para soportar agregaciones complejas con PostgreSQL.
 *
 * <p>Nótese que los valores de {@code estado} en la BD son uppercase:
 * {@code 'PAGADA'}, {@code 'ANULADA'}, {@code 'PENDIENTE'}.
 */
public interface ReporteVentasJpaRepository extends Repository<VentaEntity, Long> {

    /**
     * Retorna los totales de ventas pagadas para un día específico.
     *
     * <p>Filtra por {@code estado = 'PAGADA'} y por {@code pagado_en::date = :fecha}.
     *
     * @param fecha fecha a consultar.
     * @return proyección con conteos y sumas desglosas por método de pago.
     */
    @Query(value = """
            SELECT
                COUNT(*)                                                              AS totalVentas,
                COALESCE(SUM(v.total), 0)                                            AS montoTotal,
                COALESCE(SUM(CASE WHEN v.metodo_pago = 'EFECTIVO' THEN v.total ELSE 0 END), 0) AS montoEfectivo,
                COALESCE(SUM(CASE WHEN v.metodo_pago = 'QR'       THEN v.total ELSE 0 END), 0) AS montoQr
            FROM ventas v
            WHERE v.estado = 'PAGADA'
              AND v.pagado_en::date = :fecha
            """, nativeQuery = true)
    VentasDiariasProjection findVentasDiarias(@Param("fecha") LocalDate fecha);

    /**
     * Retorna el ranking de productos más vendidos en el período indicado.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @param limit cantidad máxima de resultados.
     * @return lista ordenada por monto total descendente.
     */
    @Query(value = """
            SELECT
                p.id              AS productoId,
                dv.producto_nombre AS nombre,
                COALESCE(p.categoria, 'OTRO') AS categoria,
                SUM(dv.cantidad)  AS cantidadVendida,
                SUM(dv.subtotal)  AS montoTotal
            FROM detalle_ventas dv
            JOIN ventas v ON v.id = dv.venta_id
            LEFT JOIN productos p ON p.id = dv.producto_id
            WHERE v.estado = 'PAGADA'
              AND v.pagado_en BETWEEN :desde AND :hasta
            GROUP BY p.id, dv.producto_nombre, p.categoria
            ORDER BY montoTotal DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TopProductoProjection> findTopProductos(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta,
            @Param("limit") int limit);

    /**
     * Retorna las ventas agrupadas por categoría de producto en el período indicado.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return lista ordenada por monto total descendente.
     */
    @Query(value = """
            SELECT
                COALESCE(p.categoria, 'OTRO') AS categoria,
                SUM(dv.cantidad)              AS cantidadVendida,
                SUM(dv.subtotal)              AS montoTotal
            FROM detalle_ventas dv
            JOIN ventas v ON v.id = dv.venta_id
            LEFT JOIN productos p ON p.id = dv.producto_id
            WHERE v.estado = 'PAGADA'
              AND v.pagado_en BETWEEN :desde AND :hasta
            GROUP BY COALESCE(p.categoria, 'OTRO')
            ORDER BY montoTotal DESC
            """, nativeQuery = true)
    List<VentasPorCategoriaProjection> findVentasPorCategoria(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);

    /**
     * Retorna el resumen de ventas (pagadas y anuladas) en el período indicado.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return proyección con conteos y suma de ventas pagadas.
     */
    @Query(value = """
            SELECT
                COUNT(*) FILTER (WHERE v.estado = 'PAGADA')                AS totalVentas,
                COALESCE(SUM(v.total) FILTER (WHERE v.estado = 'PAGADA'), 0) AS montoVentas,
                COUNT(*) FILTER (WHERE v.estado = 'ANULADA')               AS ventasAnuladas
            FROM ventas v
            WHERE v.pagado_en BETWEEN :desde AND :hasta
               OR v.anulado_en BETWEEN :desde AND :hasta
            """, nativeQuery = true)
    ResumenVentasProjection findResumenVentas(
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);
}
