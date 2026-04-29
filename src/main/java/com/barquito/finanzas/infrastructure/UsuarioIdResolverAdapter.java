package com.barquito.finanzas.infrastructure;

import com.barquito.autenticacion.infrastructure.UsuarioJpaRepository;
import com.barquito.finanzas.application.UsuarioIdResolverPort;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa {@link UsuarioIdResolverPort} para el módulo finanzas.
 *
 * <p>Resuelve el identificador de usuario a partir del nombre de login (subject del JWT).
 */
@Component("finanzasUsuarioIdResolverAdapter")
public class UsuarioIdResolverAdapter implements UsuarioIdResolverPort {

    private final UsuarioJpaRepository usuarioJpaRepository;

    /**
     * Construye el adaptador con el repositorio de usuarios.
     *
     * @param usuarioJpaRepository repositorio JPA de usuarios.
     */
    public UsuarioIdResolverAdapter(final UsuarioJpaRepository usuarioJpaRepository) {
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long resolverIdPorUsername(final String username) {
        return usuarioJpaRepository.findByNombreIgnoreCase(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado en finanzas: " + username));
    }
}
