package com.barquito.finanzas.application;

import com.barquito.finanzas.domain.TipoTransaccion;
import com.barquito.finanzas.domain.Transaccion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de respuesta que representa una transacción financiera.
 *
 * @param id          identificador único.
 * @param tipo        tipo de transacción (INGRESO o EGRESO).
 * @param monto       importe de la transacción.
 * @param descripcion descripción textual.
 * @param ventaId     id de la venta asociada; {@code null} para egresos.
 * @param usuarioId   id del usuario que registró la transacción.
 * @param fechaHora   fecha y hora de registro.
 */
public record TransaccionResponse(
        Long id,
        TipoTransaccion tipo,
        BigDecimal monto,
        String descripcion,
        Long ventaId,
        Long usuarioId,
        OffsetDateTime fechaHora
) {

    /**
     * Crea un {@link TransaccionResponse} a partir de una entidad de dominio.
     *
     * @param t la transacción de dominio.
     * @return respuesta mapeada.
     */
    public static TransaccionResponse from(final Transaccion t) {
        return new TransaccionResponse(
                t.id(),
                t.tipo(),
                t.monto(),
                t.descripcion(),
                t.ventaId(),
                t.usuarioId(),
                t.fechaHora()
        );
    }
}
