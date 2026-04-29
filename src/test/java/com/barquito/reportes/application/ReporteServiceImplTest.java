package com.barquito.reportes.application;

import com.barquito.reportes.domain.ResumenPeriodoReporte;
import com.barquito.reportes.domain.ResumenVentasData;
import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReporteServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock
    private ReporteVentasPort ventasPort;

    @Mock
    private ReporteFinanzasPort finanzasPort;

    @InjectMocks
    private ReporteServiceImpl service;

    private final OffsetDateTime desde = OffsetDateTime.parse("2026-04-01T00:00:00+00:00");
    private final OffsetDateTime hasta = OffsetDateTime.parse("2026-04-30T23:59:59+00:00");

    // =========================================================================
    // obtenerVentasDiarias
    // =========================================================================

    @Nested
    @DisplayName("obtenerVentasDiarias")
    class ObtenerVentasDiarias {

        @Test
        @DisplayName("con fecha → llama al puerto con esa fecha y retorna el reporte")
        void conFecha_llamaPortYRetornaReporte() {
            final LocalDate fecha = LocalDate.of(2026, 4, 27);
            final VentasDiariasReporte reporte = new VentasDiariasReporte(
                    fecha, 5,
                    new BigDecimal("500.00"),
                    new BigDecimal("300.00"),
                    new BigDecimal("200.00"));
            when(ventasPort.obtenerVentasDiarias(fecha)).thenReturn(reporte);

            final VentasDiariasReporte result = service.obtenerVentasDiarias(fecha);

            assertThat(result.fecha()).isEqualTo(fecha);
            assertThat(result.totalVentas()).isEqualTo(5);
            assertThat(result.montoTotal()).isEqualByComparingTo("500.00");
            assertThat(result.montoEfectivo()).isEqualByComparingTo("300.00");
            assertThat(result.montoQr()).isEqualByComparingTo("200.00");
            verify(ventasPort).obtenerVentasDiarias(fecha);
        }

        @Test
        @DisplayName("con null → usa LocalDate.now() como fecha de consulta")
        void conNull_usaFechaActual() {
            final LocalDate hoy = LocalDate.now();
            final VentasDiariasReporte reporte = new VentasDiariasReporte(
                    hoy, 0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
            when(ventasPort.obtenerVentasDiarias(any(LocalDate.class))).thenReturn(reporte);

            final VentasDiariasReporte result = service.obtenerVentasDiarias(null);

            assertThat(result.fecha()).isEqualTo(hoy);
            verify(ventasPort).obtenerVentasDiarias(hoy);
        }
    }

    // =========================================================================
    // obtenerTopProductos
    // =========================================================================

    @Nested
    @DisplayName("obtenerTopProductos")
    class ObtenerTopProductos {

        @Test
        @DisplayName("limit=10 → llama al puerto con limit=10 y retorna lista")
        void conLimitValido_retornaListaMapeada() {
            final TopProductoItem p1 = new TopProductoItem(
                    1L, "Birra IPA", "CERVEZA",
                    new BigDecimal("50.000"), new BigDecimal("375.00"));
            final TopProductoItem p2 = new TopProductoItem(
                    2L, "Vodka Premium", "ESPIRITUOSO",
                    new BigDecimal("10.000"), new BigDecimal("200.00"));
            when(ventasPort.obtenerTopProductos(desde, hasta, 10)).thenReturn(List.of(p1, p2));

            final List<TopProductoItem> result = service.obtenerTopProductos(desde, hasta, 10);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).nombre()).isEqualTo("Birra IPA");
            assertThat(result.get(0).categoria()).isEqualTo("CERVEZA");
            assertThat(result.get(0).montoTotal()).isEqualByComparingTo("375.00");
            assertThat(result.get(1).nombre()).isEqualTo("Vodka Premium");
            verify(ventasPort).obtenerTopProductos(desde, hasta, 10);
        }

        @Test
        @DisplayName("limit=60 → se aplica MAX_LIMIT=50 antes de llamar al puerto")
        void conLimitExcedido_aplicaMaxLimit() {
            when(ventasPort.obtenerTopProductos(eq(desde), eq(hasta), eq(ReporteServiceImpl.MAX_LIMIT)))
                    .thenReturn(List.of());

            service.obtenerTopProductos(desde, hasta, 60);

            verify(ventasPort).obtenerTopProductos(desde, hasta, ReporteServiceImpl.MAX_LIMIT);
        }

        @Test
        @DisplayName("limit=50 → no se aplica capping (exactamente MAX_LIMIT)")
        void conLimitExactoAlMax_noAplicaCapping() {
            when(ventasPort.obtenerTopProductos(eq(desde), eq(hasta), eq(50)))
                    .thenReturn(List.of());

            service.obtenerTopProductos(desde, hasta, 50);

            verify(ventasPort).obtenerTopProductos(desde, hasta, 50);
        }
    }

    // =========================================================================
    // obtenerVentasPorCategoria
    // =========================================================================

    @Nested
    @DisplayName("obtenerVentasPorCategoria")
    class ObtenerVentasPorCategoria {

        @Test
        @DisplayName("retorna lista desde el puerto")
        void retornaListaMapeada() {
            final VentasPorCategoriaItem p1 = new VentasPorCategoriaItem(
                    "CERVEZA", new BigDecimal("100.000"), new BigDecimal("750.00"));
            final VentasPorCategoriaItem p2 = new VentasPorCategoriaItem(
                    "ESPIRITUOSO", new BigDecimal("20.000"), new BigDecimal("400.00"));
            when(ventasPort.obtenerVentasPorCategoria(desde, hasta)).thenReturn(List.of(p1, p2));

            final List<VentasPorCategoriaItem> result = service.obtenerVentasPorCategoria(desde, hasta);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).categoria()).isEqualTo("CERVEZA");
            assertThat(result.get(0).montoTotal()).isEqualByComparingTo("750.00");
            assertThat(result.get(1).categoria()).isEqualTo("ESPIRITUOSO");
            verify(ventasPort).obtenerVentasPorCategoria(desde, hasta);
        }

        @Test
        @DisplayName("sin ventas → retorna lista vacía")
        void sinVentas_retornaListaVacia() {
            when(ventasPort.obtenerVentasPorCategoria(desde, hasta)).thenReturn(List.of());

            final List<VentasPorCategoriaItem> result = service.obtenerVentasPorCategoria(desde, hasta);

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // obtenerResumenPeriodo
    // =========================================================================

    @Nested
    @DisplayName("obtenerResumenPeriodo")
    class ObtenerResumenPeriodo {

        @Test
        @DisplayName("retorna reporte con balance calculado correctamente (ingresos - egresos)")
        void retornaResumenConBalanceCorrecto() {
            when(ventasPort.obtenerResumenVentas(desde, hasta))
                    .thenReturn(new ResumenVentasData(10L, new BigDecimal("1500.00"), 2L));
            when(finanzasPort.sumByTipoAndPeriodo("ingreso", desde, hasta))
                    .thenReturn(new BigDecimal("1800.00"));
            when(finanzasPort.sumByTipoAndPeriodo("egreso", desde, hasta))
                    .thenReturn(new BigDecimal("300.00"));

            final ResumenPeriodoReporte result = service.obtenerResumenPeriodo(desde, hasta);

            assertThat(result.desde()).isEqualTo(desde);
            assertThat(result.hasta()).isEqualTo(hasta);
            assertThat(result.totalVentas()).isEqualTo(10);
            assertThat(result.montoVentas()).isEqualByComparingTo("1500.00");
            assertThat(result.ventasAnuladas()).isEqualTo(2);
            assertThat(result.totalIngresos()).isEqualByComparingTo("1800.00");
            assertThat(result.totalEgresos()).isEqualByComparingTo("300.00");
            assertThat(result.balance()).isEqualByComparingTo("1500.00");
        }

        @Test
        @DisplayName("balance negativo cuando egresos > ingresos")
        void balanceNegativo_cuandoEgresosSuperiores() {
            when(ventasPort.obtenerResumenVentas(desde, hasta))
                    .thenReturn(new ResumenVentasData(0L, BigDecimal.ZERO, 0L));
            when(finanzasPort.sumByTipoAndPeriodo("ingreso", desde, hasta))
                    .thenReturn(new BigDecimal("100.00"));
            when(finanzasPort.sumByTipoAndPeriodo("egreso", desde, hasta))
                    .thenReturn(new BigDecimal("500.00"));

            final ResumenPeriodoReporte result = service.obtenerResumenPeriodo(desde, hasta);

            assertThat(result.balance()).isEqualByComparingTo("-400.00");
        }
    }
}
