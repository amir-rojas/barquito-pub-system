package com.barquito.pedidos.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EstadoPedido}.
 */
class EstadoPedidoTest {

    @Test
    @DisplayName("enum tiene exactamente 3 valores")
    void enum_tiene3Valores() {
        assertThat(EstadoPedido.values()).hasSize(3);
    }

    @Test
    @DisplayName("fromValue devuelve el enum correcto para mayúsculas")
    void fromValue_mayusculas_retornaEnum() {
        assertThat(EstadoPedido.fromValue("ABIERTO")).isEqualTo(EstadoPedido.ABIERTO);
        assertThat(EstadoPedido.fromValue("CERRADO")).isEqualTo(EstadoPedido.CERRADO);
        assertThat(EstadoPedido.fromValue("CANCELADO")).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    @DisplayName("fromValue es case-insensitive")
    void fromValue_minusculas_retornaEnum() {
        assertThat(EstadoPedido.fromValue("abierto")).isEqualTo(EstadoPedido.ABIERTO);
        assertThat(EstadoPedido.fromValue("cerrado")).isEqualTo(EstadoPedido.CERRADO);
        assertThat(EstadoPedido.fromValue("cancelado")).isEqualTo(EstadoPedido.CANCELADO);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor inválido")
    void fromValue_valorInvalido_lanzaException() {
        assertThatThrownBy(() -> EstadoPedido.fromValue("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("ABIERTO puede transicionar a CERRADO")
    void abierto_puedeTransicionarA_cerrado() {
        assertThat(EstadoPedido.ABIERTO.isTransitionAllowed(EstadoPedido.CERRADO)).isTrue();
    }

    @Test
    @DisplayName("ABIERTO puede transicionar a CANCELADO")
    void abierto_puedeTransicionarA_cancelado() {
        assertThat(EstadoPedido.ABIERTO.isTransitionAllowed(EstadoPedido.CANCELADO)).isTrue();
    }

    @Test
    @DisplayName("CERRADO es estado terminal — no permite transiciones")
    void cerrado_esTerminal_noPermiteTransiciones() {
        assertThat(EstadoPedido.CERRADO.isTransitionAllowed(EstadoPedido.ABIERTO)).isFalse();
        assertThat(EstadoPedido.CERRADO.isTransitionAllowed(EstadoPedido.CANCELADO)).isFalse();
    }

    @Test
    @DisplayName("CANCELADO es estado terminal — no permite transiciones")
    void cancelado_esTerminal_noPermiteTransiciones() {
        assertThat(EstadoPedido.CANCELADO.isTransitionAllowed(EstadoPedido.ABIERTO)).isFalse();
        assertThat(EstadoPedido.CANCELADO.isTransitionAllowed(EstadoPedido.CERRADO)).isFalse();
    }
}
