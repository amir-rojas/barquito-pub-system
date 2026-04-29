package com.barquito.pedidos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio Spring Data JPA para {@link PedidoEntity}.
 */
public interface PedidoJpaRepository extends JpaRepository<PedidoEntity, Long> {

    /**
     * Retorna todos los pedidos de una mesa.
     *
     * @param mesaId id de la mesa.
     * @return lista de pedidos de la mesa.
     */
    List<PedidoEntity> findByMesaId(Long mesaId);

    /**
     * Cuenta los pedidos ABIERTOS de una mesa.
     *
     * @param mesaId id de la mesa.
     * @param estado estado a filtrar (debe ser "ABIERTO").
     * @return número de pedidos en ese estado.
     */
    long countByMesaIdAndEstado(Long mesaId, String estado);

    /**
     * Verifica si existe alguna línea con estado ENTREGADO en cualquier pedido de la mesa.
     *
     * <p>Usado en la regla de cancelación de pedido para determinar si la mesa
     * debe ir a CUENTA_PEDIDA o a DISPONIBLE.
     *
     * @param mesaId id de la mesa.
     * @return {@code true} si existe al menos una línea ENTREGADO.
     */
    @Query("""
            SELECT COUNT(l) > 0
            FROM LineaPedidoEntity l
            JOIN PedidoEntity p ON p.id = l.pedidoId
            WHERE p.mesaId = :mesaId
              AND l.estado = 'ENTREGADO'
            """)
    boolean existsEntregadaLineaByMesaId(@Param("mesaId") Long mesaId);

    /**
     * Busca el pedido en estado ABIERTO de una mesa.
     *
     * @param mesaId id de la mesa.
     * @param estado estado a filtrar (debe ser "ABIERTO").
     * @return el pedido activo, o vacío.
     */
    java.util.Optional<PedidoEntity> findByMesaIdAndEstado(Long mesaId, String estado);
}
