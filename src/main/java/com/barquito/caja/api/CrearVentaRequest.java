package com.barquito.caja.api;

import jakarta.validation.constraints.NotNull;

/**
 * Request para crear una venta desde un pedido cerrado.
 *
 * @param pedidoId identificador del pedido origen; requerido.
 */
public record CrearVentaRequest(
        @NotNull(message = "pedidoId es requerido") Long pedidoId
) {}
