package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.EstadoLinea;

/**
 * Clave compuesta para el mapa de roles permitidos por transición de línea de pedido.
 *
 * <p>Usada como clave en {@code LineaPedidoServiceImpl.ROLES_PERMITIDOS}.
 *
 * @param from estado origen de la transición.
 * @param to   estado destino de la transición.
 */
public record TransitionKey(EstadoLinea from, EstadoLinea to) {}
