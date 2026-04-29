package com.barquito.caja.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Respuesta con los datos completos de una venta y sus detalles.
 *
 * @param id          identificador de la venta.
 * @param pedidoId    referencia al pedido origen.
 * @param mesaId      referencia a la mesa.
 * @param cajeroId    referencia al cajero.
 * @param total       importe total.
 * @param metodoPago  método de pago serializado como texto; {@code null} cuando PENDIENTE o ANULADA.
 * @param estado      estado serializado como texto.
 * @param creadoEn    fecha y hora de creación.
 * @param pagadoEn    fecha y hora de cobro; {@code null} si no está PAGADA.
 * @param anuladoEn   fecha y hora de anulación; {@code null} si no está ANULADA.
 * @param detalles    lista de detalles de la venta.
 */
public record VentaResponse(
        Long id,
        Long pedidoId,
        Long mesaId,
        Long cajeroId,
        BigDecimal total,
        String metodoPago,
        String estado,
        OffsetDateTime creadoEn,
        OffsetDateTime pagadoEn,
        OffsetDateTime anuladoEn,
        List<DetalleVentaResponse> detalles
) {}
