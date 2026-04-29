package com.barquito.finanzas.application;

import com.barquito.finanzas.domain.TipoTransaccion;
import com.barquito.finanzas.domain.Transaccion;
import com.barquito.finanzas.domain.TransaccionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransaccionServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TransaccionServiceImpl")
class TransaccionServiceImplTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private final OffsetDateTime ahora = OffsetDateTime.now();

    private Transaccion ingresoGuardado() {
        return new Transaccion(1L, TipoTransaccion.INGRESO,
                new BigDecimal("50.00"), "Cobro venta #10 - EFECTIVO",
                10L, 5L, ahora);
    }

    private Transaccion egresoGuardado() {
        return new Transaccion(2L, TipoTransaccion.EGRESO,
                new BigDecimal("20.00"), "Compra de insumos",
                null, 7L, ahora);
    }

    // =====================================================================
    // registrarIngreso
    // =====================================================================

    @Nested
    @DisplayName("registrarIngreso()")
    class RegistrarIngreso {

        @Test
        @DisplayName("llama repository.save y retorna TransaccionResponse con datos correctos")
        void registrarIngreso_llamaRepositoryYRetornaResponse() {
            when(transaccionRepository.save(any())).thenReturn(ingresoGuardado());

            final TransaccionResponse response = transaccionService.registrarIngreso(
                    10L, new BigDecimal("50.00"), "Cobro venta #10 - EFECTIVO", 5L);

            verify(transaccionRepository).save(argThat(t ->
                    t.tipo() == TipoTransaccion.INGRESO
                            && t.ventaId().equals(10L)
                            && t.monto().compareTo(new BigDecimal("50.00")) == 0
                            && t.usuarioId().equals(5L)
            ));
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.tipo()).isEqualTo(TipoTransaccion.INGRESO);
            assertThat(response.monto()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(response.ventaId()).isEqualTo(10L);
            assertThat(response.usuarioId()).isEqualTo(5L);
        }
    }

    // =====================================================================
    // registrarEgreso
    // =====================================================================

    @Nested
    @DisplayName("registrarEgreso()")
    class RegistrarEgreso {

        @Test
        @DisplayName("guarda con tipo EGRESO y ventaId null, retorna TransaccionResponse")
        void registrarEgreso_tipoEgresoYVentaIdNull() {
            when(transaccionRepository.save(any())).thenReturn(egresoGuardado());

            final RegistrarEgresoCommand command = new RegistrarEgresoCommand(
                    new BigDecimal("20.00"), "Compra de insumos", 7L);
            final TransaccionResponse response = transaccionService.registrarEgreso(command);

            verify(transaccionRepository).save(argThat(t ->
                    t.tipo() == TipoTransaccion.EGRESO
                            && t.ventaId() == null
                            && t.monto().compareTo(new BigDecimal("20.00")) == 0
                            && t.usuarioId().equals(7L)
            ));
            assertThat(response.id()).isEqualTo(2L);
            assertThat(response.tipo()).isEqualTo(TipoTransaccion.EGRESO);
            assertThat(response.ventaId()).isNull();
        }
    }

    // =====================================================================
    // listarTransacciones
    // =====================================================================

    @Nested
    @DisplayName("listarTransacciones()")
    class ListarTransacciones {

        @Test
        @DisplayName("retorna todas las transacciones ordenadas más recientes primero")
        void listarTransacciones_retornaTodasLasTransacciones() {
            when(transaccionRepository.findAllOrderByFechaHoraDesc())
                    .thenReturn(List.of(ingresoGuardado(), egresoGuardado()));

            final List<TransaccionResponse> result = transaccionService.listarTransacciones();

            assertThat(result).hasSize(2);
            verify(transaccionRepository).findAllOrderByFechaHoraDesc();
        }
    }

    // =====================================================================
    // listarPorTipo
    // =====================================================================

    @Nested
    @DisplayName("listarPorTipo()")
    class ListarPorTipo {

        @Test
        @DisplayName("tipo INGRESO retorna solo ingresos")
        void listarPorTipo_ingreso_retornaSoloIngresos() {
            when(transaccionRepository.findAllByTipoOrderByFechaHoraDesc(TipoTransaccion.INGRESO))
                    .thenReturn(List.of(ingresoGuardado()));

            final List<TransaccionResponse> result =
                    transaccionService.listarPorTipo(TipoTransaccion.INGRESO);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).tipo()).isEqualTo(TipoTransaccion.INGRESO);
            verify(transaccionRepository).findAllByTipoOrderByFechaHoraDesc(TipoTransaccion.INGRESO);
        }

        @Test
        @DisplayName("tipo EGRESO retorna solo egresos")
        void listarPorTipo_egreso_retornaSoloEgresos() {
            when(transaccionRepository.findAllByTipoOrderByFechaHoraDesc(TipoTransaccion.EGRESO))
                    .thenReturn(List.of(egresoGuardado()));

            final List<TransaccionResponse> result =
                    transaccionService.listarPorTipo(TipoTransaccion.EGRESO);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).tipo()).isEqualTo(TipoTransaccion.EGRESO);
            verify(transaccionRepository).findAllByTipoOrderByFechaHoraDesc(TipoTransaccion.EGRESO);
        }
    }

    // =====================================================================
    // listarPorPeriodo
    // =====================================================================

    @Nested
    @DisplayName("listarPorPeriodo()")
    class ListarPorPeriodo {

        @Test
        @DisplayName("retorna transacciones en el rango indicado")
        void listarPorPeriodo_retornaTransaccionesEnRango() {
            final OffsetDateTime desde = ahora.minusDays(7);
            final OffsetDateTime hasta = ahora;
            when(transaccionRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta))
                    .thenReturn(List.of(ingresoGuardado(), egresoGuardado()));

            final List<TransaccionResponse> result =
                    transaccionService.listarPorPeriodo(desde, hasta);

            assertThat(result).hasSize(2);
            verify(transaccionRepository).findAllByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta);
        }
    }

    // =====================================================================
    // obtenerResumen
    // =====================================================================

    @Nested
    @DisplayName("obtenerResumen()")
    class ObtenerResumen {

        @Test
        @DisplayName("retorna ResumenFinanciero con totalIngresos, totalEgresos y balance correcto")
        void obtenerResumen_retornaResumenConBalance() {
            final OffsetDateTime desde = ahora.minusDays(30);
            final OffsetDateTime hasta = ahora;

            when(transaccionRepository.sumMontoByTipoAndFechaHoraBetween(
                    eq(TipoTransaccion.INGRESO), eq(desde), eq(hasta)))
                    .thenReturn(new BigDecimal("100.00"));
            when(transaccionRepository.sumMontoByTipoAndFechaHoraBetween(
                    eq(TipoTransaccion.EGRESO), eq(desde), eq(hasta)))
                    .thenReturn(new BigDecimal("30.00"));

            final ResumenFinanciero resumen = transaccionService.obtenerResumen(desde, hasta);

            assertThat(resumen.totalIngresos()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(resumen.totalEgresos()).isEqualByComparingTo(new BigDecimal("30.00"));
            assertThat(resumen.balance()).isEqualByComparingTo(new BigDecimal("70.00"));
            assertThat(resumen.desde()).isEqualTo(desde);
            assertThat(resumen.hasta()).isEqualTo(hasta);
        }
    }
}
