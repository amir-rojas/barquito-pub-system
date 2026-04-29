package com.barquito.mesas.api;

/**
 * DTO de respuesta para zona.
 *
 * <p>Stub — implementación real pendiente (Fase 6).
 *
 * @param id          identificador de la zona.
 * @param nombre      nombre de la zona.
 * @param descripcion descripción de la zona (puede ser null).
 * @param orden       orden de presentación.
 */
public record ZonaResponse(
        Long id,
        String nombre,
        String descripcion,
        int orden
) {}
