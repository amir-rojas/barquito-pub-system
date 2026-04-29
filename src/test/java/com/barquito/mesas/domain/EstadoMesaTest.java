package com.barquito.mesas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link EstadoMesa}.
 */
class EstadoMesaTest {

    @Test
    @DisplayName("fromValue devuelve el enum correcto para valor en mayúsculas")
    void fromValue_mayusculas_retornaEnum() {
        assertThat(EstadoMesa.fromValue("DISPONIBLE")).isEqualTo(EstadoMesa.DISPONIBLE);
        assertThat(EstadoMesa.fromValue("OCUPADA")).isEqualTo(EstadoMesa.OCUPADA);
        assertThat(EstadoMesa.fromValue("CUENTA_PEDIDA")).isEqualTo(EstadoMesa.CUENTA_PEDIDA);
        assertThat(EstadoMesa.fromValue("FUSIONADA")).isEqualTo(EstadoMesa.FUSIONADA);
    }

    @Test
    @DisplayName("fromValue es case-insensitive — minúsculas también funcionan")
    void fromValue_minusculas_retornaEnum() {
        assertThat(EstadoMesa.fromValue("disponible")).isEqualTo(EstadoMesa.DISPONIBLE);
        assertThat(EstadoMesa.fromValue("ocupada")).isEqualTo(EstadoMesa.OCUPADA);
        assertThat(EstadoMesa.fromValue("cuenta_pedida")).isEqualTo(EstadoMesa.CUENTA_PEDIDA);
        assertThat(EstadoMesa.fromValue("fusionada")).isEqualTo(EstadoMesa.FUSIONADA);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor inválido")
    void fromValue_valorInvalido_lanzaException() {
        assertThatThrownBy(() -> EstadoMesa.fromValue("INEXISTENTE"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("el enum tiene exactamente 4 valores")
    void enum_tiene4Valores() {
        assertThat(EstadoMesa.values()).hasSize(4);
    }
}
