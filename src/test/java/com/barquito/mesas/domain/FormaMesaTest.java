package com.barquito.mesas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link FormaMesa}.
 */
class FormaMesaTest {

    @Test
    @DisplayName("fromValue devuelve el enum correcto para valor en mayúsculas")
    void fromValue_mayusculas_retornaEnum() {
        assertThat(FormaMesa.fromValue("CIRCULAR")).isEqualTo(FormaMesa.CIRCULAR);
        assertThat(FormaMesa.fromValue("RECTANGULAR")).isEqualTo(FormaMesa.RECTANGULAR);
    }

    @Test
    @DisplayName("fromValue es case-insensitive — minúsculas también funcionan")
    void fromValue_minusculas_retornaEnum() {
        assertThat(FormaMesa.fromValue("circular")).isEqualTo(FormaMesa.CIRCULAR);
        assertThat(FormaMesa.fromValue("rectangular")).isEqualTo(FormaMesa.RECTANGULAR);
    }

    @Test
    @DisplayName("fromValue lanza IllegalArgumentException para valor inválido")
    void fromValue_valorInvalido_lanzaException() {
        assertThatThrownBy(() -> FormaMesa.fromValue("TRIANGULAR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("el enum tiene exactamente 2 valores")
    void enum_tiene2Valores() {
        assertThat(FormaMesa.values()).hasSize(2);
    }
}
