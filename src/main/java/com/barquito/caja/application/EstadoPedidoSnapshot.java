package com.barquito.caja.application;

/**
 * Copia local del estado de pedido para el bounded context de caja.
 *
 * <p>Anti-corruption layer: caja no importa tipos del dominio de pedidos.
 * Este enum es propiedad exclusiva del contexto caja.
 */
public enum EstadoPedidoSnapshot {
    ABIERTO,
    CERRADO,
    CANCELADO
}
