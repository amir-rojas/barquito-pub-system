package com.barquito.caja.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para {@link VentaEntity}.
 */
public interface VentaJpaRepository extends JpaRepository<VentaEntity, Long> {

    /**
     * Busca la venta asociada a un pedido.
     *
     * @param pedidoId id del pedido.
     * @return la venta si existe, vacío en caso contrario.
     */
    Optional<VentaEntity> findByPedidoId(Long pedidoId);

    /**
     * Verifica si existe una venta para el pedido dado.
     *
     * @param pedidoId id del pedido.
     * @return {@code true} si existe al menos una venta para ese pedido.
     */
    boolean existsByPedidoId(Long pedidoId);
}
