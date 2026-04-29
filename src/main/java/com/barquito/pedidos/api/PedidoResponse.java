package com.barquito.pedidos.api;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * DTO de respuesta para un pedido, opcionalmente con sus líneas.
 *
 * @param id             identificador único del pedido.
 * @param mesaId         id de la mesa.
 * @param meseroId       id del mesero que creó el pedido.
 * @param estado         estado actual del pedido.
 * @param notas          notas opcionales.
 * @param creadoEn       timestamp de creación.
 * @param actualizadoEn  timestamp de última actualización.
 * @param cerradoEn      timestamp de cierre/cancelación (null si ABIERTO).
 * @param lineas         líneas del pedido (puede ser null o vacía en listados simples).
 */
public record PedidoResponse(
        Long id,
        Long mesaId,
        Long meseroId,
        String estado,
        String notas,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn,
        OffsetDateTime cerradoEn,
        List<LineaPedidoResponse> lineas
) {}
