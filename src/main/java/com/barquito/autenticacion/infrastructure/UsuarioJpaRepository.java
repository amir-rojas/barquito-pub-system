package com.barquito.autenticacion.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA para {@link UsuarioEntity}.
 *
 * <p>Usa un índice {@code LOWER(nombre)} ya creado en la BD por la migración V1.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Long> {

    /**
     * Busca un usuario ignorando mayúsculas/minúsculas en el nombre.
     *
     * <p>La query aprovecha el índice {@code usuarios_nombre_lower_idx}.
     *
     * @param nombre nombre de login a buscar.
     * @return un {@link Optional} con la entidad si existe.
     */
    @Query("SELECT u FROM UsuarioEntity u WHERE LOWER(u.nombre) = LOWER(:nombre)")
    Optional<UsuarioEntity> findByNombreIgnoreCase(@Param("nombre") String nombre);
}
