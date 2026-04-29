package com.barquito.autenticacion.infrastructure;

import com.barquito.autenticacion.domain.Rol;
import com.barquito.autenticacion.domain.Usuario;
import com.barquito.autenticacion.domain.UsuarioRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link UsuarioRepository} usando JPA.
 *
 * <p>Convierte entre {@link UsuarioEntity} (infraestructura) y {@link Usuario} (dominio).
 * La conversión del campo {@code rol} de {@code String} a {@link Rol} se realiza
 * mediante {@link Rol#fromValue(String)}.
 */
@Component
public class UsuarioJpaAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio Spring Data JPA.
     */
    public UsuarioJpaAdapter(final UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Usuario> findByNombre(final String nombre) {
        return jpaRepository.findByNombreIgnoreCase(nombre)
                .map(this::toDomain);
    }

    /**
     * Mapea una entidad JPA al objeto de dominio.
     *
     * @param entity entidad de infraestructura.
     * @return objeto de dominio {@link Usuario}.
     */
    private Usuario toDomain(final UsuarioEntity entity) {
        return new Usuario(
                entity.getId(),
                entity.getNombre(),
                entity.getPasswordHash(),
                Rol.fromValue(entity.getRol()),
                entity.isActivo()
        );
    }
}
