package com.barquito.mesas.infrastructure;

import com.barquito.mesas.domain.Zona;
import com.barquito.mesas.domain.ZonaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que implementa {@link ZonaRepository} usando JPA.
 *
 * <p>Convierte entre {@link ZonaEntity} (infraestructura) y {@link Zona} (dominio).
 * La conversión se centraliza en {@link #toDomain(ZonaEntity)}.
 */
@Component
public class ZonaJpaAdapter implements ZonaRepository {

    private final ZonaJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public ZonaJpaAdapter(final ZonaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Zona save(final Zona zona) {
        final ZonaEntity entity = new ZonaEntity(
                zona.id(),
                zona.nombre(),
                zona.descripcion(),
                zona.orden()
        );
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Zona> findById(final Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Zona> findAllOrdenadas() {
        return jpaRepository.findAllByOrderByOrdenAsc()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByNombreIgnoreCase(final String nombre) {
        return jpaRepository.existsByNombreIgnoreCase(nombre);
    }

    @Override
    public boolean existsByNombreIgnoreCaseAndIdNot(final String nombre, final Long id) {
        return jpaRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * @param entity entidad de infraestructura.
     * @return objeto de dominio {@link Zona}.
     */
    private Zona toDomain(final ZonaEntity entity) {
        return new Zona(
                entity.getId(),
                entity.getNombre(),
                entity.getDescripcion(),
                entity.getOrden()
        );
    }
}
