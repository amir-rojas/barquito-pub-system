package com.barquito.mesas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO de request para crear una mesa.
 *
 * <p>Stub — implementación real pendiente (Fase 6).
 *
 * @param numero número visible de la mesa (requerido).
 * @param zonaId id de la zona (requerido).
 * @param forma  forma física (opcional, CIRCULAR o RECTANGULAR).
 */
public record CrearMesaRequest(
        @NotBlank(message = "El número es requerido")
        String numero,
        @NotNull(message = "La zona es requerida")
        Long zonaId,
        String forma
) {}
