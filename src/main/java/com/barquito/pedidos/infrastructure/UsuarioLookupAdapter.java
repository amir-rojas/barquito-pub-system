package com.barquito.pedidos.infrastructure;

import com.barquito.autenticacion.domain.UsuarioRepository;
import com.barquito.pedidos.application.UsuarioLookupPort;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa {@link UsuarioLookupPort}.
 *
 * <p>Resuelve el id de usuario a partir del nombre de login (subject del JWT),
 * sin necesidad de modificar la estructura del token.
 */
@Component
public class UsuarioLookupAdapter implements UsuarioLookupPort {

    private final UsuarioRepository usuarioRepository;

    /**
     * Construye el adaptador con el repositorio de dominio de usuarios.
     *
     * @param usuarioRepository repositorio de dominio del módulo de autenticación.
     */
    public UsuarioLookupAdapter(final UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Long findIdByNombre(final String nombre) {
        return usuarioRepository.findByNombre(nombre)
                .map(u -> u.id())
                .orElseThrow(() -> new PedidoOperacionInvalidaException(
                        "Usuario no encontrado: " + nombre));
    }
}
