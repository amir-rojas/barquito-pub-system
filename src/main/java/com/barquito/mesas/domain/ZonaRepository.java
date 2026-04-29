package com.barquito.mesas.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (output port) del dominio de zonas.
 *
 * <p>Define el contrato de acceso a datos para zonas sin acoplarse a ningún
 * framework de persistencia. La implementación concreta vive en la capa de
 * infraestructura ({@code ZonaJpaAdapter}).
 */
public interface ZonaRepository {

    /**
     * Guarda una zona (creación o actualización).
     *
     * @param zona la zona a persistir.
     * @return la zona persistida, con el id asignado si es nueva.
     */
    Zona save(Zona zona);

    /**
     * Busca una zona por su id.
     *
     * @param id identificador de la zona.
     * @return un {@link Optional} con la zona si existe, vacío si no.
     */
    Optional<Zona> findById(Long id);

    /**
     * Retorna todas las zonas ordenadas por el campo {@code orden} ascendente.
     *
     * @return lista de zonas ordenadas.
     */
    List<Zona> findAllOrdenadas();

    /**
     * Verifica si ya existe una zona con ese nombre (case-insensitive).
     *
     * @param nombre nombre a verificar.
     * @return {@code true} si existe una zona con ese nombre.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe otra zona con ese nombre (case-insensitive) excluyendo la zona con el id dado.
     *
     * <p>Útil para la validación de unicidad al actualizar una zona: permite que la zona
     * conserve su propio nombre sin lanzar error de duplicado.
     *
     * @param nombre nombre a verificar.
     * @param id     id de la zona que se está actualizando (excluido de la búsqueda).
     * @return {@code true} si existe otra zona con ese nombre.
     */
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
