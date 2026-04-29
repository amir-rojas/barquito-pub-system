package com.barquito.mesas.api;

/**
 * DTO de request para actualizar atributos físicos de una mesa.
 *
 * <p>Stub — implementación real pendiente (Fase 6).
 * Todos los campos son opcionales (null = no cambiar).
 *
 * @param numero  nuevo número visible (null para no cambiar).
 * @param zonaId  nuevo id de zona (null para no cambiar).
 * @param forma   nueva forma (null para no cambiar).
 */
public record ActualizarMesaRequest(
        String numero,
        Long zonaId,
        String forma
) {}
