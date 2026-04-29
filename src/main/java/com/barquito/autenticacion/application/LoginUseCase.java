package com.barquito.autenticacion.application;

/**
 * Puerto de entrada (input port) para el caso de uso de autenticación.
 *
 * <p>Define el contrato que el controlador invoca sin acoplarse a la implementación.
 */
public interface LoginUseCase {

    /**
     * Autentica a un usuario y genera un token.
     *
     * @param nombre nombre de login del usuario.
     * @param pin    PIN/contraseña en texto plano.
     * @return {@link LoginResult} con el token y metadata del usuario.
     * @throws com.barquito.autenticacion.domain.CredencialesInvalidasException si las credenciales son inválidas.
     */
    LoginResult login(String nombre, String pin);
}
