package com.barquito.pedidos.api;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de request para actualizar cantidad y/o notas de una línea PENDIENTE.
 *
 * @param cantidad nueva cantidad (opcional).
 * @param notas    nuevas notas (opcional).
 */
public record ActualizarLineaPedidoRequest(
        @Positive(message = "La cantidad debe ser positiva")
        BigDecimal cantidad,
        String notas
) {}
