package com.barquito.caja.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para {@link DetalleVentaEntity}.
 */
public interface DetalleVentaJpaRepository extends JpaRepository<DetalleVentaEntity, Long> {

    /**
     * Retorna todos los detalles de una venta.
     *
     * @param ventaId id de la venta.
     * @return lista de detalles de la venta.
     */
    List<DetalleVentaEntity> findByVentaId(Long ventaId);
}
