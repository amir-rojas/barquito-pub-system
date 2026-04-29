package com.barquito.mesas.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para {@link ZonaEntity}.
 */
public interface ZonaJpaRepository extends JpaRepository<ZonaEntity, Long> {

    /**
     * Busca una zona por nombre, ignorando mayúsculas/minúsculas.
     *
     * @param nombre nombre a buscar.
     * @return zona encontrada, si existe.
     */
    Optional<ZonaEntity> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe una zona con ese nombre (case-insensitive).
     *
     * @param nombre nombre a verificar.
     * @return {@code true} si existe.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe otra zona con ese nombre (case-insensitive) excluyendo la zona con el id dado.
     *
     * @param nombre nombre a verificar.
     * @param id     id de la zona a excluir de la búsqueda.
     * @return {@code true} si existe otra zona con ese nombre.
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    /**
     * Retorna todas las zonas ordenadas por orden ascendente.
     *
     * @return lista ordenada de zonas.
     */
    List<ZonaEntity> findAllByOrderByOrdenAsc();
}
