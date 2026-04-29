package com.barquito.caja.application;

/**
 * Puerto de salida para resolver el identificador de usuario por nombre.
 *
 * <p>Copia propia del contexto caja — no importa el puerto del contexto pedidos.
 */
public interface UsuarioLookupPort {

    /**
     * Retorna el identificador persistente del usuario dado su nombre de usuario (username).
     *
     * @param username el nombre de usuario (sujeto del JWT).
     * @return el identificador {@code Long} del usuario.
     * @throws com.barquito.caja.domain.VentaOperacionInvalidaException si el usuario no existe.
     */
    Long resolverIdPorUsername(String username);
}
