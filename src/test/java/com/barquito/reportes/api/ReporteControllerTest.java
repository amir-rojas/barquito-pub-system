package com.barquito.reportes.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.reportes.application.ReporteService;
import com.barquito.reportes.domain.ResumenPeriodoReporte;
import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;
import com.barquito.shared.exception.GlobalExceptionHandler;
import com.barquito.shared.infrastructure.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link ReporteController}.
 */
@WebMvcTest(ReporteController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService reporteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/reportes";
    private static final String DESDE = "2026-04-01T00:00:00+00:00";
    private static final String HASTA = "2026-04-30T23:59:59+00:00";

    // =========================================================================
    // GET /api/reportes/ventas-diarias
    // =========================================================================

    @Nested
    @DisplayName("GET /ventas-diarias")
    class VentasDiarias {

        @Test
        @DisplayName("ADMIN sin fecha → 200 OK")
        @WithMockUser(roles = "ADMIN")
        void admin_sinFecha_retorna200() throws Exception {
            when(reporteService.obtenerVentasDiarias(any()))
                    .thenReturn(ventasDiariasReporte(LocalDate.now()));

            mockMvc.perform(get(BASE_URL + "/ventas-diarias"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalVentas").value(3));
        }

        @Test
        @DisplayName("ADMIN con fecha → 200 OK con la fecha indicada")
        @WithMockUser(roles = "ADMIN")
        void admin_conFecha_retorna200() throws Exception {
            final LocalDate fecha = LocalDate.of(2026, 4, 27);
            when(reporteService.obtenerVentasDiarias(fecha))
                    .thenReturn(ventasDiariasReporte(fecha));

            mockMvc.perform(get(BASE_URL + "/ventas-diarias").param("fecha", "2026-04-27"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.fecha").value("2026-04-27"));
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void mesero_retorna403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/ventas-diarias"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("sin autenticación → 401 Unauthorized")
        void sinAutenticacion_retorna401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/ventas-diarias"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // GET /api/reportes/top-productos
    // =========================================================================

    @Nested
    @DisplayName("GET /top-productos")
    class TopProductos {

        @Test
        @DisplayName("ADMIN → 200 OK con lista de productos")
        @WithMockUser(roles = "ADMIN")
        void admin_retorna200() throws Exception {
            when(reporteService.obtenerTopProductos(any(OffsetDateTime.class),
                    any(OffsetDateTime.class), anyInt()))
                    .thenReturn(List.of(topProductoItem()));

            mockMvc.perform(get(BASE_URL + "/top-productos")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("Birra IPA"));
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void mesero_retorna403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/top-productos")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("ADMIN sin parámetros obligatorios → 400 Bad Request")
        @WithMockUser(roles = "ADMIN")
        void sinParametros_retorna400() throws Exception {
            mockMvc.perform(get(BASE_URL + "/top-productos"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================================
    // GET /api/reportes/ventas-por-categoria
    // =========================================================================

    @Nested
    @DisplayName("GET /ventas-por-categoria")
    class VentasPorCategoria {

        @Test
        @DisplayName("ADMIN → 200 OK con lista de categorías")
        @WithMockUser(roles = "ADMIN")
        void admin_retorna200() throws Exception {
            when(reporteService.obtenerVentasPorCategoria(any(OffsetDateTime.class),
                    any(OffsetDateTime.class)))
                    .thenReturn(List.of(new VentasPorCategoriaItem(
                            "CERVEZA", new BigDecimal("100.000"), new BigDecimal("750.00"))));

            mockMvc.perform(get(BASE_URL + "/ventas-por-categoria")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].categoria").value("CERVEZA"));
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void mesero_retorna403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/ventas-por-categoria")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isForbidden());
        }
    }

    // =========================================================================
    // GET /api/reportes/resumen
    // =========================================================================

    @Nested
    @DisplayName("GET /resumen")
    class Resumen {

        @Test
        @DisplayName("ADMIN → 200 OK con resumen del período")
        @WithMockUser(roles = "ADMIN")
        void admin_retorna200() throws Exception {
            when(reporteService.obtenerResumenPeriodo(any(OffsetDateTime.class),
                    any(OffsetDateTime.class)))
                    .thenReturn(resumenPeriodoReporte());

            mockMvc.perform(get(BASE_URL + "/resumen")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalVentas").value(10))
                    .andExpect(jsonPath("$.balance").isNumber());
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void mesero_retorna403() throws Exception {
            mockMvc.perform(get(BASE_URL + "/resumen")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("sin autenticación → 401 Unauthorized")
        void sinAutenticacion_retorna401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/resumen")
                            .param("desde", DESDE)
                            .param("hasta", HASTA))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private VentasDiariasReporte ventasDiariasReporte(final LocalDate fecha) {
        return new VentasDiariasReporte(
                fecha, 3,
                new BigDecimal("150.00"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00")
        );
    }

    private TopProductoItem topProductoItem() {
        return new TopProductoItem(
                1L, "Birra IPA", "CERVEZA",
                new BigDecimal("50.000"),
                new BigDecimal("375.00")
        );
    }

    private ResumenPeriodoReporte resumenPeriodoReporte() {
        final OffsetDateTime desde = OffsetDateTime.parse("2026-04-01T00:00:00+00:00");
        final OffsetDateTime hasta = OffsetDateTime.parse("2026-04-30T23:59:59+00:00");
        return new ResumenPeriodoReporte(
                desde, hasta,
                10, new BigDecimal("1500.00"),
                2,
                new BigDecimal("1800.00"),
                new BigDecimal("300.00"),
                new BigDecimal("1500.00")
        );
    }
}
