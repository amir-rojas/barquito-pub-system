package com.barquito.caja.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link MetodoPago}.
 */
class MetodoPagoTest {

    @Test
    @DisplayName("enum tiene exactamente 2 valores")
    void enum_tiene2Valores() {
        assertThat(MetodoPago.values()).hasSize(2);
    }

    @Test
    @DisplayName("fromValue devuelve EFECTIVO para mayúsculas")
    void fromValue_efectivoMayusculas_retornaEnum() {
        assertThat(MetodoPago.fromValue("EFECTIVO")).isEqualTo(MetodoPago.EFECTIVO);
    }

    @Test
    @DisplayName("fromValue devuelve EFECTIVO para minúsculas")
    void fromValue_efectivoMinusculas_retornaEnum() {
        assertThat(MetodoPago.fromValue("efectivo")).isEqualTo(MetodoPago.EFECTIVO);
    }

    @Test
    @DisplayName("fromValue devuelve QR para mayúsculas")
    void fromValue_qrMayusculas_retornaEnum() {
        assertThat(MetodoPago.fromValue("QR")).isEqualTo(MetodoPago.QR);
    }

    @Test
    @DisplayName("fromValue devuelve QR para minúsculas")
    void fromValue_qrMinusculas_retornaEnum() {
        assertThat(MetodoPago.fromValue("qr")).isEqualTo(MetodoPago.QR);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor inválido")
    void fromValue_valorInvalido_lanzaException() {
        assertThatThrownBy(() -> MetodoPago.fromValue("BITCOIN"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor vacío")
    void fromValue_vacio_lanzaException() {
        assertThatThrownBy(() -> MetodoPago.fromValue(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
