package com.barquito.caja.infrastructure;

import com.barquito.caja.domain.DetalleVenta;
import com.barquito.caja.domain.DetalleVentaRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adaptador de salida que implementa {@link DetalleVentaRepository} usando JPA.
 *
 * <p>El método {@link #saveAll} realiza un flush explícito después de insertar para
 * que Hibernate re-lea el campo {@code subtotal} generado por la BD
 * ({@code GENERATED ALWAYS AS (cantidad * precio_unitario) STORED}).
 */
@Component
public class DetalleVentaJpaAdapter implements DetalleVentaRepository {

    private final DetalleVentaJpaRepository jpaRepository;
    private final EntityManager entityManager;

    /**
     * Construye el adaptador con sus dependencias.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     * @param entityManager EntityManager para flush post-INSERT.
     */
    public DetalleVentaJpaAdapter(final DetalleVentaJpaRepository jpaRepository,
                                  final EntityManager entityManager) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<DetalleVenta> saveAll(final List<DetalleVenta> detalles) {
        final List<DetalleVentaEntity> entities = detalles.stream()
                .map(this::toEntity)
                .toList();
        final List<DetalleVentaEntity> saved = jpaRepository.saveAll(entities);
        // Flush para que Hibernate re-lea el campo @Generated subtotal desde la BD
        entityManager.flush();
        return saved.stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<DetalleVenta> findByVentaId(final Long ventaId) {
        return jpaRepository.findByVentaId(ventaId).stream()
                .map(this::toDomain)
                .toList();
    }

    /**
     * Mapea un objeto de dominio a entidad JPA.
     * El campo {@code subtotal} NO se incluye — es calculado por la BD.
     *
     * @param d objeto de dominio.
     * @return entidad JPA.
     */
    private DetalleVentaEntity toEntity(final DetalleVenta d) {
        return new DetalleVentaEntity(
                d.id(),
                d.ventaId(),
                d.productoId(),
                d.productoNombre(),
                d.cantidad(),
                d.precioUnitario()
        );
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * @param e entidad de infraestructura.
     * @return objeto de dominio {@link DetalleVenta}.
     */
    private DetalleVenta toDomain(final DetalleVentaEntity e) {
        return new DetalleVenta(
                e.getId(),
                e.getVentaId(),
                e.getProductoId(),
                e.getProductoNombre(),
                e.getCantidad(),
                e.getPrecioUnitario(),
                e.getSubtotal()
        );
    }
}
