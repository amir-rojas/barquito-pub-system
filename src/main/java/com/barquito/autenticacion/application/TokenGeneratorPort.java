package com.barquito.autenticacion.application;

import com.barquito.autenticacion.domain.Rol;

/**
 * Puerto de salida para la generación de tokens de autenticación.
 *
 * <p>Aísla la capa de aplicación de la implementación concreta JWT (infraestructura).
 */
public interface TokenGeneratorPort {

    /**
     * Genera un token firmado para el usuario dado.
     *
     * @param nombre nombre de login del usuario (subject del token).
     * @param rol    rol del usuario.
     * @return token compacto firmado.
     */
    String generarToken(String nombre, Rol rol);

    /**
     * Retorna el tiempo de expiración del token en milisegundos.
     *
     * @return expiración en ms.
     */
    long getExpirationMs();
}
