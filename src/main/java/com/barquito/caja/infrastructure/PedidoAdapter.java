package com.barquito.caja.infrastructure;

import com.barquito.caja.application.EstadoLineaSnapshot;
import com.barquito.caja.application.EstadoPedidoSnapshot;
import com.barquito.caja.application.LineaPedidoSnapshot;
import com.barquito.caja.application.PedidoPort;
import com.barquito.caja.application.PedidoSnapshot;
import com.barquito.pedidos.infrastructure.LineaPedidoEntity;
import com.barquito.pedidos.infrastructure.LineaPedidoJpaRepository;
import com.barquito.pedidos.infrastructure.PedidoJpaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que implementa {@link PedidoPort}.
 *
 * <p>Anti-corruption layer: caja lee datos de pedidos directamente desde las entidades
 * de infraestructura de pedidos (cross-module read-only access). Esta es la ÚNICA clase
 * en {@code caja.infrastructure} autorizada a importar de {@code pedidos.infrastructure}.
 *
 * <p>El join con {@code productos} para obtener {@code productoNombre} se hace
 * mediante native query batch-fetch para evitar N+1 y sin duplicar el @Entity de productos.
 */
@Component
public class PedidoAdapter implements PedidoPort {

    private final PedidoJpaRepository pedidoJpaRepository;
    private final LineaPedidoJpaRepository lineaPedidoJpaRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * Construye el adaptador con sus dependencias.
     *
     * @param pedidoJpaRepository       repositorio de pedidos.
     * @param lineaPedidoJpaRepository  repositorio de líneas de pedido.
     */
    public PedidoAdapter(final PedidoJpaRepository pedidoJpaRepository,
                         final LineaPedidoJpaRepository lineaPedidoJpaRepository) {
        this.pedidoJpaRepository = pedidoJpaRepository;
        this.lineaPedidoJpaRepository = lineaPedidoJpaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PedidoSnapshot> findSnapshot(final Long pedidoId) {
        return pedidoJpaRepository.findById(pedidoId)
                .map(e -> new PedidoSnapshot(
                        e.getId(),
                        e.getMesaId(),
                        EstadoPedidoSnapshot.valueOf(e.getEstado().toUpperCase())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<LineaPedidoSnapshot> findLineasByPedidoId(final Long pedidoId) {
        final List<LineaPedidoEntity> lineas = lineaPedidoJpaRepository.findByPedidoId(pedidoId);
        if (lineas.isEmpty()) {
            return List.of();
        }

        final Set<Long> productoIds = lineas.stream()
                .map(LineaPedidoEntity::getProductoId)
                .collect(Collectors.toSet());

        @SuppressWarnings("unchecked")
        final List<Object[]> rows = em.createNativeQuery(
                        "SELECT id, nombre FROM productos WHERE id IN :ids")
                .setParameter("ids", productoIds)
                .getResultList();

        final Map<Long, String> nombreById = rows.stream()
                .collect(Collectors.toMap(
                        r -> ((Number) r[0]).longValue(),
                        r -> (String) r[1]));

        return lineas.stream()
                .map(l -> new LineaPedidoSnapshot(
                        l.getId(),
                        l.getProductoId(),
                        nombreById.getOrDefault(l.getProductoId(), "(producto eliminado)"),
                        l.getCantidad(),
                        l.getPrecioUnitario(),
                        l.getSubtotal(),
                        EstadoLineaSnapshot.valueOf(l.getEstado().toUpperCase())))
                .toList();
    }
}
