package com.barquito.mesas.api;

import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para fusionar una mesa secundaria bajo una principal.
 *
 * <p>Stub — implementación real pendiente (Fase 6).
 *
 * @param secundariaId id de la mesa que actuará como secundaria.
 */
public record FusionarMesaRequest(
        @NotNull(message = "El id de la mesa secundaria es requerido")
        Long secundariaId
) {}
