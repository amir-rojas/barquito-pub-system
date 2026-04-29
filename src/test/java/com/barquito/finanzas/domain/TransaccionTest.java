package com.barquito.finanzas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Transaccion} domain record.
 */
@DisplayName("Transaccion domain record")
class TransaccionTest {

    private final OffsetDateTime ahora = OffsetDateTime.now();

    @Test
    @DisplayName("constructor crea registro INGRESO con ventaId")
    void constructor_ingreso_conVentaId() {
        final Transaccion t = new Transaccion(
                1L,
                TipoTransaccion.INGRESO,
                new BigDecimal("50.00"),
                "Cobro venta #1 - EFECTIVO",
                10L,
                5L,
                ahora
        );

        assertThat(t.id()).isEqualTo(1L);
        assertThat(t.tipo()).isEqualTo(TipoTransaccion.INGRESO);
        assertThat(t.monto()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(t.descripcion()).isEqualTo("Cobro venta #1 - EFECTIVO");
        assertThat(t.ventaId()).isEqualTo(10L);
        assertThat(t.usuarioId()).isEqualTo(5L);
        assertThat(t.fechaHora()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("constructor crea registro EGRESO sin ventaId (null)")
    void constructor_egreso_sinVentaId() {
        final Transaccion t = new Transaccion(
                2L,
                TipoTransaccion.EGRESO,
                new BigDecimal("20.00"),
                "Compra de insumos",
                null,
                7L,
                ahora
        );

        assertThat(t.id()).isEqualTo(2L);
        assertThat(t.tipo()).isEqualTo(TipoTransaccion.EGRESO);
        assertThat(t.monto()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(t.descripcion()).isEqualTo("Compra de insumos");
        assertThat(t.ventaId()).isNull();
        assertThat(t.usuarioId()).isEqualTo(7L);
        assertThat(t.fechaHora()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("TipoTransaccion tiene exactamente 2 valores: INGRESO y EGRESO")
    void tipoTransaccion_tieneExactamente2Valores() {
        final TipoTransaccion[] values = TipoTransaccion.values();

        assertThat(values).hasSize(2);
        assertThat(values).containsExactlyInAnyOrder(
                TipoTransaccion.INGRESO,
                TipoTransaccion.EGRESO
        );
    }
}
