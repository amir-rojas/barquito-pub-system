package com.barquito.pedidos.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de request para agregar una línea a un pedido.
 *
 * @param productoId id del producto.
 * @param cantidad   cantidad pedida (debe ser positiva).
 * @param notas      notas opcionales de la línea.
 */
public record CrearLineaPedidoRequest(
        @NotNull(message = "El id del producto es requerido")
        Long productoId,
        @NotNull(message = "La cantidad es requerida")
        @Positive(message = "La cantidad debe ser positiva")
        BigDecimal cantidad,
        String notas
) {}
