package com.barquito.finanzas.infrastructure;

import com.barquito.finanzas.domain.TipoTransaccion;
import com.barquito.finanzas.domain.Transaccion;
import com.barquito.finanzas.domain.TransaccionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Adaptador de salida que implementa {@link TransaccionRepository} usando JPA.
 *
 * <p>Convierte entre objetos de dominio ({@link Transaccion}) y entidades JPA
 * ({@link TransaccionEntity}). Los valores de tipo se convierten de enum Java
 * a String lowercase para la base de datos (e.g. {@code INGRESO} → {@code "ingreso"}).
 */
@Component("finanzasTransaccionJpaAdapter")
public class TransaccionJpaAdapter implements TransaccionRepository {

    private final TransaccionJpaRepository jpaRepository;

    /**
     * Inyección por constructor.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public TransaccionJpaAdapter(final TransaccionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaccion save(final Transaccion transaccion) {
        final TransaccionEntity entity = TransaccionEntity.toEntity(transaccion);
        return jpaRepository.save(entity).toDomain();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaccion> findAllOrderByFechaHoraDesc() {
        return jpaRepository.findAllByOrderByFechaHoraDesc().stream()
                .map(TransaccionEntity::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaccion> findAllByTipoOrderByFechaHoraDesc(final TipoTransaccion tipo) {
        return jpaRepository.findAllByTipoOrderByFechaHoraDesc(tipo.name().toLowerCase()).stream()
                .map(TransaccionEntity::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaccion> findAllByFechaHoraBetweenOrderByFechaHoraDesc(
            final OffsetDateTime desde, final OffsetDateTime hasta) {
        return jpaRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta).stream()
                .map(TransaccionEntity::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Transaccion> findAllOrderByFechaHoraDescPaged(final int page, final int size) {
        final PageRequest pageable = PageRequest.of(page, size, Sort.by("fechaHora").descending());
        return jpaRepository.findAllByOrderByFechaHoraDesc(pageable).stream()
                .map(TransaccionEntity::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal sumMontoByTipoAndFechaHoraBetween(
            final TipoTransaccion tipo,
            final OffsetDateTime desde,
            final OffsetDateTime hasta) {
        return jpaRepository.sumMontoByTipoAndFechaHoraBetween(
                tipo.name().toLowerCase(), desde, hasta);
    }
}
