package com.barquito.finanzas.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Puerto de salida (repository) para el agregado {@link Transaccion}.
 *
 * <p>Define el contrato de persistencia desde el punto de vista del dominio.
 * La implementación vive en la capa de infraestructura.
 */
public interface TransaccionRepository {

    /**
     * Persiste una transacción y retorna la instancia guardada (con id asignado).
     *
     * @param transaccion transacción a guardar.
     * @return transacción persistida con id asignado.
     */
    Transaccion save(Transaccion transaccion);

    /**
     * Retorna todas las transacciones ordenadas por fecha descendente (más recientes primero).
     *
     * @return lista de transacciones.
     */
    List<Transaccion> findAllOrderByFechaHoraDesc();

    /**
     * Retorna todas las transacciones de un tipo específico, ordenadas por fecha descendente.
     *
     * @param tipo tipo de transacción a filtrar.
     * @return lista filtrada de transacciones.
     */
    List<Transaccion> findAllByTipoOrderByFechaHoraDesc(TipoTransaccion tipo);

    /**
     * Retorna todas las transacciones cuya fecha esté entre {@code desde} y {@code hasta},
     * ordenadas por fecha descendente.
     *
     * @param desde fecha de inicio del período (inclusive).
     * @param hasta fecha de fin del período (inclusive).
     * @return lista de transacciones en el rango.
     */
    List<Transaccion> findAllByFechaHoraBetweenOrderByFechaHoraDesc(OffsetDateTime desde, OffsetDateTime hasta);

    /**
     * Suma los montos de las transacciones de un tipo en un período dado.
     *
     * @param tipo  tipo de transacción.
     * @param desde fecha de inicio del período (inclusive).
     * @param hasta fecha de fin del período (inclusive).
     * @return suma de montos; {@code BigDecimal.ZERO} si no hay transacciones.
     */
    BigDecimal sumMontoByTipoAndFechaHoraBetween(TipoTransaccion tipo, OffsetDateTime desde, OffsetDateTime hasta);

    /**
     * Retorna una página de transacciones ordenadas por fecha descendente.
     *
     * @param page número de página (0-based).
     * @param size tamaño de la página.
     * @return lista de transacciones de la página solicitada.
     */
    List<Transaccion> findAllOrderByFechaHoraDescPaged(int page, int size);
}
