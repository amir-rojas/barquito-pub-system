package com.barquito.caja.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Venta} domain record.
 */
class VentaTest {

    private final OffsetDateTime ahora = OffsetDateTime.now();
    private Venta ventaPendiente;
    private Venta ventaPagada;
    private Venta ventaAnulada;

    @BeforeEach
    void setUp() {
        ventaPendiente = new Venta(
                1L, 10L, 3L, 5L,
                new BigDecimal("22.50"),
                null, EstadoVenta.PENDIENTE,
                ahora, null, null
        );
        ventaPagada = new Venta(
                2L, 10L, 3L, 5L,
                new BigDecimal("22.50"),
                MetodoPago.EFECTIVO, EstadoVenta.PAGADA,
                ahora, ahora, null
        );
        ventaAnulada = new Venta(
                3L, 10L, 3L, 5L,
                new BigDecimal("22.50"),
                null, EstadoVenta.ANULADA,
                ahora, null, ahora
        );
    }

    // =====================================================================
    // cobrar
    // =====================================================================

    @Test
    @DisplayName("cobrar en PENDIENTE retorna nueva venta con estado PAGADA")
    void cobrar_pendiente_retornaVentaPagada() {
        final OffsetDateTime momentoPago = OffsetDateTime.now();

        final Venta cobrada = ventaPendiente.cobrar(MetodoPago.EFECTIVO, momentoPago);

        assertThat(cobrada.estado()).isEqualTo(EstadoVenta.PAGADA);
        assertThat(cobrada.metodoPago()).isEqualTo(MetodoPago.EFECTIVO);
        assertThat(cobrada.pagadoEn()).isEqualTo(momentoPago);
        assertThat(cobrada.anuladoEn()).isNull();
    }

    @Test
    @DisplayName("cobrar en PENDIENTE preserva los demás campos del registro original")
    void cobrar_pendiente_preservaCamposIdentidad() {
        final Venta cobrada = ventaPendiente.cobrar(MetodoPago.QR, ahora);

        assertThat(cobrada.id()).isEqualTo(ventaPendiente.id());
        assertThat(cobrada.pedidoId()).isEqualTo(ventaPendiente.pedidoId());
        assertThat(cobrada.mesaId()).isEqualTo(ventaPendiente.mesaId());
        assertThat(cobrada.cajeroId()).isEqualTo(ventaPendiente.cajeroId());
        assertThat(cobrada.total()).isEqualByComparingTo(ventaPendiente.total());
        assertThat(cobrada.creadoEn()).isEqualTo(ventaPendiente.creadoEn());
    }

    @Test
    @DisplayName("cobrar en PAGADA lanza VentaOperacionInvalidaException")
    void cobrar_pagada_lanzaException() {
        assertThatThrownBy(() -> ventaPagada.cobrar(MetodoPago.EFECTIVO, ahora))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("cobrar en ANULADA lanza VentaOperacionInvalidaException")
    void cobrar_anulada_lanzaException() {
        assertThatThrownBy(() -> ventaAnulada.cobrar(MetodoPago.EFECTIVO, ahora))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("cobrar con metodoPago null lanza VentaOperacionInvalidaException")
    void cobrar_metodoPagoNull_lanzaException() {
        assertThatThrownBy(() -> ventaPendiente.cobrar(null, ahora))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    // =====================================================================
    // anular
    // =====================================================================

    @Test
    @DisplayName("anular en PENDIENTE retorna nueva venta con estado ANULADA")
    void anular_pendiente_retornaVentaAnulada() {
        final OffsetDateTime momentoAnulacion = OffsetDateTime.now();

        final Venta anulada = ventaPendiente.anular(momentoAnulacion);

        assertThat(anulada.estado()).isEqualTo(EstadoVenta.ANULADA);
        assertThat(anulada.anuladoEn()).isEqualTo(momentoAnulacion);
        assertThat(anulada.metodoPago()).isNull();
        assertThat(anulada.pagadoEn()).isNull();
    }

    @Test
    @DisplayName("anular en PENDIENTE preserva los demás campos del registro original")
    void anular_pendiente_preservaCamposIdentidad() {
        final Venta anulada = ventaPendiente.anular(ahora);

        assertThat(anulada.id()).isEqualTo(ventaPendiente.id());
        assertThat(anulada.pedidoId()).isEqualTo(ventaPendiente.pedidoId());
        assertThat(anulada.mesaId()).isEqualTo(ventaPendiente.mesaId());
        assertThat(anulada.cajeroId()).isEqualTo(ventaPendiente.cajeroId());
        assertThat(anulada.total()).isEqualByComparingTo(ventaPendiente.total());
        assertThat(anulada.creadoEn()).isEqualTo(ventaPendiente.creadoEn());
    }

    @Test
    @DisplayName("anular en PAGADA lanza VentaOperacionInvalidaException")
    void anular_pagada_lanzaException() {
        assertThatThrownBy(() -> ventaPagada.anular(ahora))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("anular en ANULADA lanza VentaOperacionInvalidaException")
    void anular_anulada_lanzaException() {
        assertThatThrownBy(() -> ventaAnulada.anular(ahora))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    // =====================================================================
    // immutability
    // =====================================================================

    @Test
    @DisplayName("cobrar no muta el registro original (inmutabilidad del record)")
    void cobrar_noMutaRegistroOriginal() {
        final EstadoVenta estadoOriginal = ventaPendiente.estado();
        final MetodoPago metodoPagoOriginal = ventaPendiente.metodoPago();

        ventaPendiente.cobrar(MetodoPago.EFECTIVO, ahora);

        assertThat(ventaPendiente.estado()).isEqualTo(estadoOriginal);
        assertThat(ventaPendiente.metodoPago()).isEqualTo(metodoPagoOriginal);
    }

    @Test
    @DisplayName("anular no muta el registro original (inmutabilidad del record)")
    void anular_noMutaRegistroOriginal() {
        final EstadoVenta estadoOriginal = ventaPendiente.estado();
        final OffsetDateTime anuladoEnOriginal = ventaPendiente.anuladoEn();

        ventaPendiente.anular(ahora);

        assertThat(ventaPendiente.estado()).isEqualTo(estadoOriginal);
        assertThat(ventaPendiente.anuladoEn()).isEqualTo(anuladoEnOriginal);
    }
}
