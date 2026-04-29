package com.barquito.caja.application;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para consultar datos de pedidos desde el contexto caja.
 *
 * <p>Anti-corruption layer: caja lee pedidos mediante este puerto sin importar
 * tipos del dominio de pedidos. Las implementaciones viven en la capa de infraestructura.
 */
public interface PedidoPort {

    /**
     * Retorna un snapshot del pedido indicado.
     *
     * <p>Nunca lanza excepción si el pedido no existe — retorna {@link Optional#empty()}.
     * El caller decide qué excepción lanzar.
     *
     * @param pedidoId el identificador del pedido.
     * @return snapshot del pedido si existe, vacío en caso contrario.
     */
    Optional<PedidoSnapshot> findSnapshot(Long pedidoId);

    /**
     * Retorna todas las líneas de un pedido sin filtrar por estado.
     *
     * <p>El caller aplica los filtros necesarios (por ejemplo, excluir CANCELADO).
     * Retorna lista vacía si el pedido no tiene líneas.
     *
     * @param pedidoId el identificador del pedido.
     * @return lista de snapshots de líneas; vacía si no hay ninguna.
     */
    List<LineaPedidoSnapshot> findLineasByPedidoId(Long pedidoId);
}
