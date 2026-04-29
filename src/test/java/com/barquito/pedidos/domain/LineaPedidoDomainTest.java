package com.barquito.pedidos.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LineaPedido} domain record.
 */
class LineaPedidoDomainTest {

    @Test
    @DisplayName("LineaPedido se construye correctamente con todos sus campos")
    void lineaPedido_construyeConTodosLosCampos() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido linea = new LineaPedido(
                1L, 2L, 3L,
                new BigDecimal("2.000"),
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                EstadoLinea.PENDIENTE,
                "sin sal",
                ahora, ahora
        );

        assertThat(linea.id()).isEqualTo(1L);
        assertThat(linea.pedidoId()).isEqualTo(2L);
        assertThat(linea.productoId()).isEqualTo(3L);
        assertThat(linea.cantidad()).isEqualByComparingTo("2.000");
        assertThat(linea.precioUnitario()).isEqualByComparingTo("10.00");
        assertThat(linea.subtotal()).isEqualByComparingTo("20.00");
        assertThat(linea.estado()).isEqualTo(EstadoLinea.PENDIENTE);
        assertThat(linea.notas()).isEqualTo("sin sal");
        assertThat(linea.creadoEn()).isEqualTo(ahora);
        assertThat(linea.actualizadoEn()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("LineaPedido record equality funciona correctamente")
    void lineaPedido_equality() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido l1 = new LineaPedido(
                1L, 2L, 3L,
                new BigDecimal("1.000"), new BigDecimal("5.00"), new BigDecimal("5.00"),
                EstadoLinea.PENDIENTE, null, ahora, ahora
        );
        final LineaPedido l2 = new LineaPedido(
                1L, 2L, 3L,
                new BigDecimal("1.000"), new BigDecimal("5.00"), new BigDecimal("5.00"),
                EstadoLinea.PENDIENTE, null, ahora, ahora
        );

        assertThat(l1).isEqualTo(l2);
    }

    @Test
    @DisplayName("LineaPedido con subtotal nulo (antes de persistir) se construye correctamente")
    void lineaPedido_subtotalNulo_construyeCorrectamente() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido linea = new LineaPedido(
                null, 2L, 3L,
                new BigDecimal("1.000"), new BigDecimal("5.00"), null,
                EstadoLinea.PENDIENTE, null, ahora, ahora
        );

        assertThat(linea.id()).isNull();
        assertThat(linea.subtotal()).isNull();
    }
}
