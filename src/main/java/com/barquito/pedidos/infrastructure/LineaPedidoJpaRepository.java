package com.barquito.pedidos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para {@link LineaPedidoEntity}.
 */
public interface LineaPedidoJpaRepository extends JpaRepository<LineaPedidoEntity, Long> {

    /**
     * Retorna todas las líneas de un pedido.
     *
     * @param pedidoId id del pedido.
     * @return lista de líneas del pedido.
     */
    List<LineaPedidoEntity> findByPedidoId(Long pedidoId);

    /**
     * Retorna las líneas de un pedido filtradas por estados.
     *
     * @param pedidoId id del pedido.
     * @param estados  lista de estados a incluir.
     * @return lista de líneas filtradas.
     */
    List<LineaPedidoEntity> findByPedidoIdAndEstadoIn(Long pedidoId, List<String> estados);
}
