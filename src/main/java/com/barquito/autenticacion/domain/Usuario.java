package com.barquito.autenticacion.domain;

/**
 * Entidad de dominio que representa a un usuario del sistema.
 *
 * <p>Inmutable por diseño: al ser un record no tiene setters ni estado mutable.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * @param id           identificador único del usuario.
 * @param nombre       nombre de login del usuario.
 * @param passwordHash hash BCrypt de la contraseña/PIN.
 * @param rol          rol que determina los permisos del usuario.
 * @param activo       indica si el usuario está habilitado para ingresar.
 */
public record Usuario(
        Long id,
        String nombre,
        String passwordHash,
        Rol rol,
        boolean activo
) {}
