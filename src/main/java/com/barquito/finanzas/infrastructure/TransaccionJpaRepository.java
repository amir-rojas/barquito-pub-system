package com.barquito.finanzas.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repositorio Spring Data JPA para {@link TransaccionEntity}.
 *
 * <p>Proporciona las operaciones de consulta sobre la tabla
 * {@code transacciones_financieras}. El tipo se filtra como {@code String}
 * (valores lowercase: {@code "ingreso"} / {@code "egreso"}).
 */
public interface TransaccionJpaRepository extends JpaRepository<TransaccionEntity, Long> {

    /**
     * Retorna todas las transacciones ordenadas por {@code fecha_hora} descendente.
     *
     * @return lista de entidades.
     */
    List<TransaccionEntity> findAllByOrderByFechaHoraDesc();

    /**
     * Retorna una página de transacciones ordenadas por {@code fecha_hora} descendente.
     *
     * @param pageable parámetros de paginación.
     * @return lista de entidades de la página solicitada.
     */
    List<TransaccionEntity> findAllByOrderByFechaHoraDesc(Pageable pageable);

    /**
     * Retorna todas las transacciones de un tipo dado, ordenadas por fecha descendente.
     *
     * @param tipo tipo como String lowercase ({@code "ingreso"} o {@code "egreso"}).
     * @return lista filtrada de entidades.
     */
    List<TransaccionEntity> findAllByTipoOrderByFechaHoraDesc(String tipo);

    /**
     * Retorna todas las transacciones en el rango de fechas, ordenadas por fecha descendente.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return lista de entidades en el rango.
     */
    List<TransaccionEntity> findAllByFechaHoraBetweenOrderByFechaHoraDesc(
            OffsetDateTime desde, OffsetDateTime hasta);

    /**
     * Suma los montos de las transacciones de un tipo en un período dado.
     *
     * <p>Retorna {@code 0} si no hay transacciones que coincidan (via COALESCE).
     *
     * @param tipo  tipo como String lowercase.
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return suma total o {@code 0} si no hay registros.
     */
    @Query("SELECT COALESCE(SUM(t.monto), 0) FROM TransaccionEntity t " +
            "WHERE t.tipo = :tipo AND t.fechaHora BETWEEN :desde AND :hasta")
    BigDecimal sumMontoByTipoAndFechaHoraBetween(
            @Param("tipo") String tipo,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);
}
