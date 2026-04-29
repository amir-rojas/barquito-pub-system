package com.barquito.autenticacion.api;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para la operación de login.
 *
 * @param nombre nombre de login del usuario; no puede estar en blanco.
 * @param pin    PIN/contraseña del usuario; no puede estar en blanco.
 */
public record LoginRequest(
        @NotBlank String nombre,
        @NotBlank String pin
) {}
