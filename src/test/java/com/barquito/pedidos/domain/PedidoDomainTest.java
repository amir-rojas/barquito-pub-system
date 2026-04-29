package com.barquito.pedidos.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Pedido} domain record.
 */
class PedidoDomainTest {

    @Test
    @DisplayName("Pedido se construye correctamente con todos sus campos")
    void pedido_construyeConTodosLosCampos() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido pedido = new Pedido(1L, 2L, 3L, EstadoPedido.ABIERTO, "Notas", ahora, ahora, null);

        assertThat(pedido.id()).isEqualTo(1L);
        assertThat(pedido.mesaId()).isEqualTo(2L);
        assertThat(pedido.meseroId()).isEqualTo(3L);
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ABIERTO);
        assertThat(pedido.notas()).isEqualTo("Notas");
        assertThat(pedido.creadoEn()).isEqualTo(ahora);
        assertThat(pedido.actualizadoEn()).isEqualTo(ahora);
        assertThat(pedido.cerradoEn()).isNull();
    }

    @Test
    @DisplayName("Pedido permite cerradoEn no nulo para estados terminales")
    void pedido_cerradoEn_noNulo() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido pedido = new Pedido(1L, 2L, 3L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);

        assertThat(pedido.cerradoEn()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("Pedido record equality funciona correctamente")
    void pedido_equality() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido p1 = new Pedido(1L, 2L, 3L, EstadoPedido.ABIERTO, null, ahora, ahora, null);
        final Pedido p2 = new Pedido(1L, 2L, 3L, EstadoPedido.ABIERTO, null, ahora, ahora, null);

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    @DisplayName("Pedido con notas nulas se construye correctamente")
    void pedido_notasNulas_construyeCorrectamente() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido pedido = new Pedido(1L, 2L, 3L, EstadoPedido.ABIERTO, null, ahora, ahora, null);

        assertThat(pedido.notas()).isNull();
    }
}
