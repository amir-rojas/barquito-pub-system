package com.barquito.pedidos.application;

/**
 * Puerto de salida para resolver el id de un usuario a partir de su nombre de login.
 *
 * <p>El JWT solo lleva el nombre (subject) y el rol; este puerto resuelve
 * el {@code userId} para registrarlo en el pedido sin modificar la estructura del token.
 */
public interface UsuarioLookupPort {

    /**
     * Retorna el id del usuario con el nombre de login dado.
     *
     * @param nombre nombre de login del usuario (principal del JWT).
     * @return id del usuario.
     * @throws com.barquito.pedidos.domain.PedidoOperacionInvalidaException
     *         si el usuario no existe.
     */
    Long findIdByNombre(String nombre);
}
