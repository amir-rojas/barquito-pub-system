package com.barquito.caja.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad de dominio que representa una venta generada desde un pedido cerrado.
 *
 * <p>Inmutable por diseño: record sin setters ni estado mutable.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * <p>Las transiciones de estado se realizan mediante los métodos de dominio
 * {@link #cobrar(MetodoPago, OffsetDateTime)} y {@link #anular(OffsetDateTime)},
 * que devuelven un nuevo registro sin mutar el original.
 *
 * @param id          identificador único de la venta.
 * @param pedidoId    referencia al pedido origen.
 * @param mesaId      referencia a la mesa asociada.
 * @param cajeroId    referencia al usuario que genera la venta.
 * @param total       importe total de la venta (suma de subtotales de detalles).
 * @param metodoPago  método de pago utilizado; {@code null} cuando PENDIENTE o ANULADA.
 * @param estado      estado operativo actual de la venta.
 * @param creadoEn    fecha y hora de creación.
 * @param pagadoEn    fecha y hora de cobro; {@code null} si no está PAGADA.
 * @param anuladoEn   fecha y hora de anulación; {@code null} si no está ANULADA.
 */
public record Venta(
        Long id,
        Long pedidoId,
        Long mesaId,
        Long cajeroId,
        BigDecimal total,
        MetodoPago metodoPago,
        EstadoVenta estado,
        OffsetDateTime creadoEn,
        OffsetDateTime pagadoEn,
        OffsetDateTime anuladoEn
) {

    /**
     * Devuelve una nueva {@link Venta} en estado PAGADA.
     *
     * <p>Transición pura de dominio: no realiza E/S.
     *
     * @param metodoPago método de pago utilizado; no puede ser {@code null}.
     * @param ahora      instante del cobro.
     * @return nueva instancia de {@link Venta} con estado PAGADA.
     * @throws VentaOperacionInvalidaException si el estado actual no permite la transición,
     *                                         o si {@code metodoPago} es {@code null}.
     */
    public Venta cobrar(final MetodoPago metodoPago, final OffsetDateTime ahora) {
        if (!this.estado.isTransitionAllowed(EstadoVenta.PAGADA)) {
            throw new VentaOperacionInvalidaException(
                    "No se puede cobrar una venta en estado " + this.estado);
        }
        if (metodoPago == null) {
            throw new VentaOperacionInvalidaException(
                    "metodoPago es requerido para cobrar una venta");
        }
        return new Venta(id, pedidoId, mesaId, cajeroId, total,
                metodoPago, EstadoVenta.PAGADA, creadoEn, ahora, null);
    }

    /**
     * Devuelve una nueva {@link Venta} en estado ANULADA.
     *
     * <p>Transición pura de dominio: no realiza E/S ni libera la mesa.
     *
     * @param ahora instante de la anulación.
     * @return nueva instancia de {@link Venta} con estado ANULADA.
     * @throws VentaOperacionInvalidaException si el estado actual no permite la transición.
     */
    public Venta anular(final OffsetDateTime ahora) {
        if (!this.estado.isTransitionAllowed(EstadoVenta.ANULADA)) {
            throw new VentaOperacionInvalidaException(
                    "No se puede anular una venta en estado " + this.estado);
        }
        return new Venta(id, pedidoId, mesaId, cajeroId, total,
                null, EstadoVenta.ANULADA, creadoEn, null, ahora);
    }
}
