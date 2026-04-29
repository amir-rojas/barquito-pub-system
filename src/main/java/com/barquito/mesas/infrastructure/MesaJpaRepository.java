package com.barquito.mesas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para {@link MesaEntity}.
 */
public interface MesaJpaRepository extends JpaRepository<MesaEntity, Long> {

    /**
     * Retorna todas las mesas con {@code activa = true}.
     *
     * @return lista de mesas activas.
     */
    List<MesaEntity> findAllByActivaTrue();

    /**
     * Retorna mesas activas de una zona específica.
     *
     * @param zonaId id de la zona.
     * @return lista de mesas activas en la zona.
     */
    List<MesaEntity> findAllByZonaIdAndActivaTrue(Long zonaId);

    /**
     * Retorna mesas secundarias activas que apuntan a la mesa principal dada.
     *
     * @param mesaPrincipalId id de la mesa principal.
     * @return lista de secundarias activas.
     */
    List<MesaEntity> findAllByMesaPrincipalIdAndActivaTrue(Long mesaPrincipalId);
}
