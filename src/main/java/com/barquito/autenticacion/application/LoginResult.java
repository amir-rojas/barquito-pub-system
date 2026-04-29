package com.barquito.autenticacion.application;

/**
 * Resultado del caso de uso de login.
 *
 * <p>Pertenece a la capa de aplicación. El controlador lo mapea a {@code TokenResponse} (API layer).
 *
 * @param token        token de autenticación generado.
 * @param id           identificador del usuario autenticado.
 * @param rol          nombre del rol del usuario autenticado.
 * @param nombre       nombre de login del usuario autenticado.
 * @param expirationMs tiempo de expiración del token en milisegundos.
 */
public record LoginResult(String token, Long id, String rol, String nombre, long expirationMs) {}
