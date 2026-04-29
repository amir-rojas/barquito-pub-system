package com.barquito.caja.infrastructure;

import com.barquito.caja.domain.EstadoVenta;
import com.barquito.caja.domain.MetodoPago;
import com.barquito.caja.domain.Venta;
import com.barquito.caja.domain.VentaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link VentaRepository} usando JPA.
 *
 * <p>Convierte entre {@link VentaEntity} (infraestructura) y {@link Venta} (dominio).
 * La conversión de texto a enum se centraliza en {@link #toDomain(VentaEntity)}.
 * NUNCA se usa {@code @Enumerated} en la entidad.
 */
@Component
public class VentaJpaAdapter implements VentaRepository {

    private final VentaJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public VentaJpaAdapter(final VentaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Venta save(final Venta venta) {
        return toDomain(jpaRepository.save(toEntity(venta)));
    }

    @Override
    public Optional<Venta> findById(final Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Venta> findByPedidoId(final Long pedidoId) {
        return jpaRepository.findByPedidoId(pedidoId).map(this::toDomain);
    }

    @Override
    public boolean existsByPedidoId(final Long pedidoId) {
        return jpaRepository.existsByPedidoId(pedidoId);
    }

    /**
     * Mapea un objeto de dominio a entidad JPA.
     *
     * @param v objeto de dominio.
     * @return entidad JPA.
     */
    private VentaEntity toEntity(final Venta v) {
        return new VentaEntity(
                v.id(),
                v.pedidoId(),
                v.mesaId(),
                v.cajeroId(),
                v.total(),
                v.metodoPago() == null ? null : v.metodoPago().name(),
                v.estado().name(),
                v.creadoEn(),
                v.pagadoEn(),
                v.anuladoEn()
        );
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * @param e entidad de infraestructura.
     * @return objeto de dominio {@link Venta}.
     */
    private Venta toDomain(final VentaEntity e) {
        return new Venta(
                e.getId(),
                e.getPedidoId(),
                e.getMesaId(),
                e.getCajeroId(),
                e.getTotal(),
                e.getMetodoPago() == null ? null : MetodoPago.fromValue(e.getMetodoPago()),
                EstadoVenta.fromValue(e.getEstado()),
                e.getCreadoEn(),
                e.getPagadoEn(),
                e.getAnuladoEn()
        );
    }
}
