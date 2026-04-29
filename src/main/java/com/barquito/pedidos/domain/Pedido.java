package com.barquito.pedidos.domain;

import java.time.OffsetDateTime;

/**
 * Entidad de dominio que representa un pedido de mesa.
 *
 * <p>Inmutable por diseño: record sin setters ni estado mutable.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * @param id             identificador único del pedido.
 * @param mesaId         referencia a la mesa del pedido.
 * @param meseroId       referencia al usuario (mesero) que creó el pedido.
 * @param estado         estado operativo actual del pedido.
 * @param notas          notas opcionales del pedido.
 * @param creadoEn       fecha y hora de creación.
 * @param actualizadoEn  fecha y hora de última actualización.
 * @param cerradoEn      fecha y hora de cierre/cancelación; {@code null} si ABIERTO.
 */
public record Pedido(
        Long id,
        Long mesaId,
        Long meseroId,
        EstadoPedido estado,
        String notas,
        OffsetDateTime creadoEn,
        OffsetDateTime actualizadoEn,
        OffsetDateTime cerradoEn
) {}
