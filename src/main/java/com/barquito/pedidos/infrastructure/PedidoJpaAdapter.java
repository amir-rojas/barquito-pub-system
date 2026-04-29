package com.barquito.pedidos.infrastructure;

import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.pedidos.domain.PedidoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link PedidoRepository} usando JPA.
 *
 * <p>Convierte entre {@link PedidoEntity} (infraestructura) y {@link Pedido} (dominio).
 * La conversión de texto a enum se centraliza en {@link #toDomain(PedidoEntity)}.
 * NUNCA se usa {@code @Enumerated} en la entidad.
 */
@Component
public class PedidoJpaAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public PedidoJpaAdapter(final PedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pedido save(final Pedido pedido) {
        final PedidoEntity entity = toEntity(pedido);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Pedido> findById(final Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Pedido> findByMesaId(final Long mesaId) {
        return jpaRepository.findByMesaId(mesaId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public long countAbiertosByMesaId(final Long mesaId) {
        return jpaRepository.countByMesaIdAndEstado(mesaId, "ABIERTO");
    }

    @Override
    public boolean existsEntregadaLineaByMesaId(final Long mesaId) {
        return jpaRepository.existsEntregadaLineaByMesaId(mesaId);
    }

    @Override
    public Optional<Pedido> findAbiertoByMesaId(final Long mesaId) {
        return jpaRepository.findByMesaIdAndEstado(mesaId, "ABIERTO").map(this::toDomain);
    }

    @Override
    public List<Pedido> findByMesaIdAndEstado(final Long mesaId, final EstadoPedido estado) {
        return jpaRepository.findByMesaIdAndEstado(mesaId, estado.name())
                .map(entity -> List.of(toDomain(entity)))
                .orElse(List.of());
    }

    /**
     * Mapea un objeto de dominio a entidad JPA.
     *
     * @param pedido objeto de dominio.
     * @return entidad JPA.
     */
    private PedidoEntity toEntity(final Pedido pedido) {
        return new PedidoEntity(
                pedido.id(),
                pedido.mesaId(),
                pedido.meseroId(),
                pedido.estado().name(),
                pedido.notas(),
                pedido.creadoEn(),
                pedido.actualizadoEn(),
                pedido.cerradoEn()
        );
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * @param entity entidad de infraestructura.
     * @return objeto de dominio {@link Pedido}.
     */
    private Pedido toDomain(final PedidoEntity entity) {
        return new Pedido(
                entity.getId(),
                entity.getMesaId(),
                entity.getMeseroId(),
                EstadoPedido.fromValue(entity.getEstado()),
                entity.getNotas(),
                entity.getCreadoEn(),
                entity.getActualizadoEn(),
                entity.getCerradoEn()
        );
    }
}
