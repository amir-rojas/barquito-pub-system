package com.barquito.caja.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EstadoVenta}.
 */
class EstadoVentaTest {

    @Test
    @DisplayName("enum tiene exactamente 3 valores")
    void enum_tiene3Valores() {
        assertThat(EstadoVenta.values()).hasSize(3);
    }

    @Test
    @DisplayName("fromValue devuelve el enum correcto para mayúsculas")
    void fromValue_mayusculas_retornaEnum() {
        assertThat(EstadoVenta.fromValue("PENDIENTE")).isEqualTo(EstadoVenta.PENDIENTE);
        assertThat(EstadoVenta.fromValue("PAGADA")).isEqualTo(EstadoVenta.PAGADA);
        assertThat(EstadoVenta.fromValue("ANULADA")).isEqualTo(EstadoVenta.ANULADA);
    }

    @Test
    @DisplayName("fromValue es case-insensitive")
    void fromValue_minusculas_retornaEnum() {
        assertThat(EstadoVenta.fromValue("pendiente")).isEqualTo(EstadoVenta.PENDIENTE);
        assertThat(EstadoVenta.fromValue("pagada")).isEqualTo(EstadoVenta.PAGADA);
        assertThat(EstadoVenta.fromValue("anulada")).isEqualTo(EstadoVenta.ANULADA);
    }

    @Test
    @DisplayName("fromValue es case-insensitive con mixed case")
    void fromValue_mixedCase_retornaEnum() {
        assertThat(EstadoVenta.fromValue("Pendiente")).isEqualTo(EstadoVenta.PENDIENTE);
        assertThat(EstadoVenta.fromValue("Pagada")).isEqualTo(EstadoVenta.PAGADA);
        assertThat(EstadoVenta.fromValue("Anulada")).isEqualTo(EstadoVenta.ANULADA);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor inválido")
    void fromValue_valorInvalido_lanzaException() {
        assertThatThrownBy(() -> EstadoVenta.fromValue("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PENDIENTE puede transicionar a PAGADA")
    void pendiente_puedeTransicionarA_pagada() {
        assertThat(EstadoVenta.PENDIENTE.isTransitionAllowed(EstadoVenta.PAGADA)).isTrue();
    }

    @Test
    @DisplayName("PENDIENTE puede transicionar a ANULADA")
    void pendiente_puedeTransicionarA_anulada() {
        assertThat(EstadoVenta.PENDIENTE.isTransitionAllowed(EstadoVenta.ANULADA)).isTrue();
    }

    @Test
    @DisplayName("PENDIENTE no puede transicionar a sí misma")
    void pendiente_noTransicinaASiMisma() {
        assertThat(EstadoVenta.PENDIENTE.isTransitionAllowed(EstadoVenta.PENDIENTE)).isFalse();
    }

    @Test
    @DisplayName("PAGADA es estado terminal — no permite transiciones")
    void pagada_esTerminal_noPermiteTransiciones() {
        assertThat(EstadoVenta.PAGADA.isTransitionAllowed(EstadoVenta.PENDIENTE)).isFalse();
        assertThat(EstadoVenta.PAGADA.isTransitionAllowed(EstadoVenta.PAGADA)).isFalse();
        assertThat(EstadoVenta.PAGADA.isTransitionAllowed(EstadoVenta.ANULADA)).isFalse();
    }

    @Test
    @DisplayName("ANULADA es estado terminal — no permite transiciones")
    void anulada_esTerminal_noPermiteTransiciones() {
        assertThat(EstadoVenta.ANULADA.isTransitionAllowed(EstadoVenta.PENDIENTE)).isFalse();
        assertThat(EstadoVenta.ANULADA.isTransitionAllowed(EstadoVenta.PAGADA)).isFalse();
        assertThat(EstadoVenta.ANULADA.isTransitionAllowed(EstadoVenta.ANULADA)).isFalse();
    }
}
