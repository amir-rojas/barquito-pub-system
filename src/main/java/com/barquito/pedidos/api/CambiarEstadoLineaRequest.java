package com.barquito.pedidos.api;

import com.barquito.pedidos.domain.EstadoLinea;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para cambiar el estado de una línea de pedido.
 *
 * @param estado nuevo estado destino de la línea.
 */
public record CambiarEstadoLineaRequest(
        @NotNull(message = "El estado es requerido")
        EstadoLinea estado
) {}
