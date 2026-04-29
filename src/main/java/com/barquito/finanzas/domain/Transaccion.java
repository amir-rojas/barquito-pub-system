package com.barquito.finanzas.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad de dominio que representa una transacción financiera.
 *
 * <p>Inmutable por diseño: record sin setters ni estado mutable.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * @param id          identificador único de la transacción.
 * @param tipo        tipo de transacción (INGRESO o EGRESO).
 * @param monto       importe de la transacción (siempre positivo).
 * @param descripcion descripción textual de la transacción.
 * @param ventaId     referencia a la venta origen; {@code null} para egresos manuales.
 * @param usuarioId   referencia al usuario que registró la transacción; puede ser {@code null}.
 * @param fechaHora   fecha y hora de la transacción; {@code null} si aún no se ha persistido.
 */
public record Transaccion(
        Long id,
        TipoTransaccion tipo,
        BigDecimal monto,
        String descripcion,
        Long ventaId,
        Long usuarioId,
        OffsetDateTime fechaHora
) {}
