package com.barquito.pedidos.infrastructure;

import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.LineaPedidoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link LineaPedidoRepository} usando JPA.
 *
 * <p>El campo {@code subtotal} es generado por la BD; se lee como readonly
 * ({@code insertable=false, updatable=false} en la entidad). Hibernate lo
 * re-lee después de cada INSERT/UPDATE gracias a {@code @Generated}.
 */
@Component
public class LineaPedidoJpaAdapter implements LineaPedidoRepository {

    private final LineaPedidoJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public LineaPedidoJpaAdapter(final LineaPedidoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LineaPedido save(final LineaPedido linea) {
        return toDomain(jpaRepository.save(toEntity(linea)));
    }

    @Override
    public Optional<LineaPedido> findById(final Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<LineaPedido> findByPedidoId(final Long pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<LineaPedido> saveAll(final List<LineaPedido> lineas) {
        final List<LineaPedidoEntity> entities = lineas.stream()
                .map(this::toEntity)
                .toList();
        return jpaRepository.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Mapea un objeto de dominio a entidad JPA.
     *
     * @param linea objeto de dominio.
     * @return entidad JPA.
     */
    private LineaPedidoEntity toEntity(final LineaPedido linea) {
        return new LineaPedidoEntity(
                linea.id(),
                linea.pedidoId(),
                linea.productoId(),
                linea.cantidad(),
                linea.precioUnitario(),
                linea.estado().name(),
                linea.notas(),
                linea.creadoEn(),
                linea.actualizadoEn()
        );
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * @param entity entidad de infraestructura.
     * @return objeto de dominio {@link LineaPedido}.
     */
    private LineaPedido toDomain(final LineaPedidoEntity entity) {
        return new LineaPedido(
                entity.getId(),
                entity.getPedidoId(),
                entity.getProductoId(),
                entity.getCantidad(),
                entity.getPrecioUnitario(),
                entity.getSubtotal(),
                EstadoLinea.fromValue(entity.getEstado()),
                entity.getNotas(),
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
