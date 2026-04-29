package com.barquito.mesas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de request para crear una zona.
 *
 * <p>Stub — implementación real pendiente (Fase 6).
 *
 * @param nombre      nombre de la zona (requerido, 1-100 chars).
 * @param descripcion descripción opcional.
 * @param orden       orden de presentación (por defecto 0).
 */
public record CrearZonaRequest(
        @NotBlank(message = "El nombre es requerido")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,
        String descripcion,
        int orden
) {}
