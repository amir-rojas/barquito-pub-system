package com.barquito.mesas.infrastructure;

import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.FormaMesa;
import com.barquito.mesas.domain.Mesa;
import com.barquito.mesas.domain.MesaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que implementa {@link MesaRepository} usando JPA.
 *
 * <p>Convierte entre {@link MesaEntity} (infraestructura) y {@link Mesa} (dominio).
 * La conversión de {@code estado} y {@code forma} de texto a enum se realiza mediante
 * {@link EstadoMesa#fromValue(String)} y {@link FormaMesa#fromValue(String)}.
 * NUNCA se usa {@code @Enumerated} en la entidad.
 */
@Component
public class MesaJpaAdapter implements MesaRepository {

    private final MesaJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public MesaJpaAdapter(final MesaJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Mesa save(final Mesa mesa) {
        final MesaEntity entity = new MesaEntity(
                mesa.id(),
                mesa.numero(),
                mesa.estado().name(),
                mesa.activa(),
                mesa.zonaId(),
                mesa.forma() != null ? mesa.forma().name() : null,
                mesa.mesaPrincipalId()
        );
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Mesa> findById(final Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Mesa> findAllActivas() {
        return jpaRepository.findAllByActivaTrue()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Mesa> findAllActivasByZonaId(final Long zonaId) {
        return jpaRepository.findAllByZonaIdAndActivaTrue(zonaId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Mesa> findSecundariasByMesaPrincipalId(final Long mesaPrincipalId) {
        return jpaRepository.findAllByMesaPrincipalIdAndActivaTrue(mesaPrincipalId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * <p>La conversión de texto a enum se centraliza aquí, siguiendo el patrón del módulo
     * de autenticación. {@code forma} puede ser null si no fue especificada.
     *
     * @param entity entidad de infraestructura.
     * @return objeto de dominio {@link Mesa}.
     */
    private Mesa toDomain(final MesaEntity entity) {
        return new Mesa(
                entity.getId(),
                entity.getNumero(),
                EstadoMesa.fromValue(entity.getEstado()),
                entity.isActiva(),
                entity.getZonaId(),
                entity.getForma() != null ? FormaMesa.fromValue(entity.getForma()) : null,
                entity.getMesaPrincipalId()
        );
    }
}
