package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.Pedido;

import java.util.List;

/**
 * Proyección de respuesta que agrega un pedido con sus líneas.
 *
 * @param pedido el pedido.
 * @param lineas las líneas del pedido.
 */
public record PedidoConLineas(
        Pedido pedido,
        List<LineaPedido> lineas
) {}
