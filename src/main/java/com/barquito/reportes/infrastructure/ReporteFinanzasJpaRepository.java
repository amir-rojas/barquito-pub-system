package com.barquito.reportes.infrastructure;

import com.barquito.finanzas.infrastructure.TransaccionEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Repositorio JPA de solo lectura para reportes de transacciones financieras.
 *
 * <p>Reutiliza {@link TransaccionEntity} (tabla {@code transacciones_financieras})
 * sin modificar la entidad original del módulo {@code finanzas}.
 */
public interface ReporteFinanzasJpaRepository extends Repository<TransaccionEntity, Long> {

    /**
     * Retorna la suma de transacciones de un tipo específico en el período indicado.
     *
     * <p>Los valores de {@code tipo} en la BD son lowercase: {@code 'ingreso'} o {@code 'egreso'}.
     *
     * @param tipo  tipo de transacción ({@code "ingreso"} o {@code "egreso"}).
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return suma de montos, o {@code 0} si no hay transacciones.
     */
    @Query(value = """
            SELECT COALESCE(SUM(t.monto), 0)
            FROM transacciones_financieras t
            WHERE t.tipo = CAST(:tipo AS tipo_transaccion)
              AND t.fecha_hora BETWEEN :desde AND :hasta
            """, nativeQuery = true)
    BigDecimal sumByTipoAndPeriodo(
            @Param("tipo") String tipo,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);
}
