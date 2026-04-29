package com.barquito.mesas.api;

/**
 * DTO de respuesta para mesa.
 *
 * @param id               identificador de la mesa.
 * @param numero           número visible de la mesa.
 * @param estado           estado operativo.
 * @param activa           si la mesa está habilitada.
 * @param zonaId           id de la zona.
 * @param forma            forma física (puede ser null).
 * @param mesaPrincipalId  id de la mesa principal si es secundaria (puede ser null).
 */
public record MesaResponse(
        Long id,
        String numero,
        String estado,
        boolean activa,
        Long zonaId,
        String forma,
        Long mesaPrincipalId
) {}
