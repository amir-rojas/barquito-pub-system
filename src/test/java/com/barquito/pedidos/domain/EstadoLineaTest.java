package com.barquito.pedidos.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EstadoLinea}.
 */
class EstadoLineaTest {

    @Test
    @DisplayName("enum tiene exactamente 5 valores")
    void enum_tiene5Valores() {
        assertThat(EstadoLinea.values()).hasSize(5);
    }

    @Test
    @DisplayName("fromValue devuelve el enum correcto para mayúsculas")
    void fromValue_mayusculas_retornaEnum() {
        assertThat(EstadoLinea.fromValue("PENDIENTE")).isEqualTo(EstadoLinea.PENDIENTE);
        assertThat(EstadoLinea.fromValue("EN_PREPARACION")).isEqualTo(EstadoLinea.EN_PREPARACION);
        assertThat(EstadoLinea.fromValue("LISTO")).isEqualTo(EstadoLinea.LISTO);
        assertThat(EstadoLinea.fromValue("ENTREGADO")).isEqualTo(EstadoLinea.ENTREGADO);
        assertThat(EstadoLinea.fromValue("CANCELADO")).isEqualTo(EstadoLinea.CANCELADO);
    }

    @Test
    @DisplayName("fromValue es case-insensitive")
    void fromValue_minusculas_retornaEnum() {
        assertThat(EstadoLinea.fromValue("pendiente")).isEqualTo(EstadoLinea.PENDIENTE);
        assertThat(EstadoLinea.fromValue("en_preparacion")).isEqualTo(EstadoLinea.EN_PREPARACION);
        assertThat(EstadoLinea.fromValue("listo")).isEqualTo(EstadoLinea.LISTO);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor inválido")
    void fromValue_valorInvalido_lanzaException() {
        assertThatThrownBy(() -> EstadoLinea.fromValue("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("PENDIENTE puede transicionar a EN_PREPARACION")
    void pendiente_puedeTransicionarA_enPreparacion() {
        assertThat(EstadoLinea.PENDIENTE.isTransitionAllowed(EstadoLinea.EN_PREPARACION)).isTrue();
    }

    @Test
    @DisplayName("PENDIENTE puede transicionar a CANCELADO")
    void pendiente_puedeTransicionarA_cancelado() {
        assertThat(EstadoLinea.PENDIENTE.isTransitionAllowed(EstadoLinea.CANCELADO)).isTrue();
    }

    @Test
    @DisplayName("EN_PREPARACION puede transicionar a LISTO")
    void enPreparacion_puedeTransicionarA_listo() {
        assertThat(EstadoLinea.EN_PREPARACION.isTransitionAllowed(EstadoLinea.LISTO)).isTrue();
    }

    @Test
    @DisplayName("EN_PREPARACION puede transicionar a CANCELADO")
    void enPreparacion_puedeTransicionarA_cancelado() {
        assertThat(EstadoLinea.EN_PREPARACION.isTransitionAllowed(EstadoLinea.CANCELADO)).isTrue();
    }

    @Test
    @DisplayName("LISTO puede transicionar a ENTREGADO")
    void listo_puedeTransicionarA_entregado() {
        assertThat(EstadoLinea.LISTO.isTransitionAllowed(EstadoLinea.ENTREGADO)).isTrue();
    }

    @Test
    @DisplayName("LISTO puede transicionar a CANCELADO")
    void listo_puedeTransicionarA_cancelado() {
        assertThat(EstadoLinea.LISTO.isTransitionAllowed(EstadoLinea.CANCELADO)).isTrue();
    }

    @Test
    @DisplayName("ENTREGADO es terminal — no permite transiciones")
    void entregado_esTerminal_noPermiteTransiciones() {
        assertThat(EstadoLinea.ENTREGADO.isTransitionAllowed(EstadoLinea.PENDIENTE)).isFalse();
        assertThat(EstadoLinea.ENTREGADO.isTransitionAllowed(EstadoLinea.EN_PREPARACION)).isFalse();
        assertThat(EstadoLinea.ENTREGADO.isTransitionAllowed(EstadoLinea.LISTO)).isFalse();
        assertThat(EstadoLinea.ENTREGADO.isTransitionAllowed(EstadoLinea.CANCELADO)).isFalse();
    }

    @Test
    @DisplayName("CANCELADO es terminal — no permite transiciones")
    void cancelado_esTerminal_noPermiteTransiciones() {
        assertThat(EstadoLinea.CANCELADO.isTransitionAllowed(EstadoLinea.PENDIENTE)).isFalse();
        assertThat(EstadoLinea.CANCELADO.isTransitionAllowed(EstadoLinea.EN_PREPARACION)).isFalse();
        assertThat(EstadoLinea.CANCELADO.isTransitionAllowed(EstadoLinea.LISTO)).isFalse();
        assertThat(EstadoLinea.CANCELADO.isTransitionAllowed(EstadoLinea.ENTREGADO)).isFalse();
    }

    @Test
    @DisplayName("transiciones inválidas son rechazadas")
    void transicionesInvalidas_sonRechazadas() {
        assertThat(EstadoLinea.PENDIENTE.isTransitionAllowed(EstadoLinea.LISTO)).isFalse();
        assertThat(EstadoLinea.PENDIENTE.isTransitionAllowed(EstadoLinea.ENTREGADO)).isFalse();
        assertThat(EstadoLinea.EN_PREPARACION.isTransitionAllowed(EstadoLinea.PENDIENTE)).isFalse();
        assertThat(EstadoLinea.EN_PREPARACION.isTransitionAllowed(EstadoLinea.ENTREGADO)).isFalse();
        assertThat(EstadoLinea.LISTO.isTransitionAllowed(EstadoLinea.PENDIENTE)).isFalse();
        assertThat(EstadoLinea.LISTO.isTransitionAllowed(EstadoLinea.EN_PREPARACION)).isFalse();
    }
}
