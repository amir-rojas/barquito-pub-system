package com.barquito.finanzas.application;

/**
 * Puerto de salida para resolver el identificador de usuario por nombre de login.
 *
 * <p>Permite que la capa de API obtenga el {@code usuarioId} a partir del
 * subject (nombre) del JWT autenticado, sin importar infraestructura directamente.
 */
public interface UsuarioIdResolverPort {

    /**
     * Retorna el identificador persistente del usuario dado su nombre de login.
     *
     * @param username nombre de usuario (subject del JWT).
     * @return identificador {@code Long} del usuario.
     * @throws IllegalStateException si el usuario no existe.
     */
    Long resolverIdPorUsername(String username);
}
