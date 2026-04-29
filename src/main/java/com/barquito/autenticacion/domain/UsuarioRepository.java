package com.barquito.autenticacion.domain;

import java.util.Optional;

/**
 * Puerto de salida (output port) del dominio de autenticación.
 *
 * <p>Define el contrato de acceso a datos para usuarios sin acoplarse a ningún
 * framework de persistencia. La implementación concreta vive en la capa de
 * infraestructura.
 */
public interface UsuarioRepository {

    /**
     * Busca un usuario por su nombre de forma case-insensitive.
     *
     * @param nombre nombre de login a buscar.
     * @return un {@link Optional} con el usuario si existe, vacío si no.
     */
    Optional<Usuario> findByNombre(String nombre);
}
