package com.barquito.autenticacion.api;

/**
 * DTO de respuesta al login exitoso.
 *
 * <p>Contrato consumido por el frontend SPA.
 *
 * @param token     JWT firmado para incluir en cabeceras {@code Authorization: Bearer}.
 * @param expiresAt instante de expiración del token en formato ISO-8601.
 * @param usuario   datos básicos del usuario autenticado.
 */
public record TokenResponse(
        String token,
        String expiresAt,
        UsuarioResponse usuario
) {

    /**
     * Datos básicos del usuario autenticado incluidos en la respuesta de login.
     *
     * @param id     identificador del usuario.
     * @param nombre nombre de login del usuario.
     * @param rol    rol del usuario en mayúsculas (e.g. {@code "ADMIN"}).
     */
    public record UsuarioResponse(Long id, String nombre, String rol) {}
}
