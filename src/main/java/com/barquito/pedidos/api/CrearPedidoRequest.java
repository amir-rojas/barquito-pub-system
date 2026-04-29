package com.barquito.pedidos.api;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para crear un nuevo pedido.
 *
 * @param mesaId id de la mesa donde se crea el pedido.
 * @param notas  notas opcionales del pedido.
 */
public record CrearPedidoRequest(
        @NotNull(message = "El id de la mesa es requerido")
        Long mesaId,
        String notas
) {}
