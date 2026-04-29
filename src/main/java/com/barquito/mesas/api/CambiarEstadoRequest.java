package com.barquito.mesas.api;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de request para cambiar el estado de una mesa.
 *
 * <p>Stub — implementación real pendiente (Fase 6).
 *
 * @param estado nuevo estado (DISPONIBLE, OCUPADA o CUENTA_PEDIDA — no FUSIONADA).
 */
public record CambiarEstadoRequest(
        @NotBlank(message = "El estado es requerido")
        String estado
) {}
