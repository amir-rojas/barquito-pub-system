package com.barquito.caja.infrastructure;

import com.barquito.autenticacion.domain.UsuarioRepository;
import com.barquito.caja.application.UsuarioLookupPort;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa {@link UsuarioLookupPort}.
 *
 * <p>Resuelve el id de usuario a partir del nombre de login (subject del JWT),
 * sin necesidad de modificar la estructura del token.
 * Usa la misma convención que {@code pedidos.infrastructure.UsuarioLookupAdapter}
 * pero lanza {@link VentaOperacionInvalidaException} en lugar de la excepción de pedidos.
 */
@Component("cajaUsuarioLookupAdapter")
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
    public Long resolverIdPorUsername(final String username) {
        return usuarioRepository.findByNombre(username)
                .map(u -> u.id())
                .orElseThrow(() -> new VentaOperacionInvalidaException(
                        "Usuario no encontrado: " + username));
    }
}
