package com.barquito.pedidos.api;

/**
 * DTO de request para actualizar las notas de un pedido.
 *
 * @param notas nuevas notas del pedido.
 */
public record ActualizarPedidoRequest(String notas) {}
