package com.barquito.caja.api;

import com.barquito.caja.domain.MetodoPago;
import jakarta.validation.constraints.NotNull;

/**
 * Request para cobrar una venta indicando el método de pago.
 *
 * @param metodoPago método de pago utilizado; requerido.
 */
public record CobrarVentaRequest(
        @NotNull(message = "metodoPago es requerido") MetodoPago metodoPago
) {}
