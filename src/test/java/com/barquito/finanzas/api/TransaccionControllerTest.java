package com.barquito.finanzas.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.finanzas.application.ResumenFinanciero;
import com.barquito.finanzas.application.TransaccionResponse;
import com.barquito.finanzas.application.TransaccionService;
import com.barquito.finanzas.application.UsuarioIdResolverPort;
import com.barquito.finanzas.domain.TipoTransaccion;
import com.barquito.shared.exception.GlobalExceptionHandler;
import com.barquito.shared.infrastructure.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link TransaccionController}.
 */
@WebMvcTest(TransaccionController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("TransaccionController")
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransaccionService transaccionService;

    @MockitoBean
    private UsuarioIdResolverPort usuarioIdResolverPort;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/finanzas";
    private final OffsetDateTime ahora = OffsetDateTime.now();

    private TransaccionResponse egresoResponse() {
        return new TransaccionResponse(
                2L, TipoTransaccion.EGRESO,
                new BigDecimal("20.00"), "Compra de insumos",
                null, 7L, ahora);
    }

    private TransaccionResponse ingresoResponse() {
        return new TransaccionResponse(
                1L, TipoTransaccion.INGRESO,
                new BigDecimal("50.00"), "Cobro venta #10 - EFECTIVO",
                10L, 5L, ahora);
    }

    // =====================================================================
    // POST /api/finanzas/egresos
    // =====================================================================

    @Test
    @DisplayName("POST /api/finanzas/egresos con ADMIN → 201 Created")
    @WithMockUser(username = "admin1", roles = "ADMIN")
    void registrarEgreso_conAdmin_retorna201() throws Exception {
        when(usuarioIdResolverPort.resolverIdPorUsername("admin1")).thenReturn(7L);
        when(transaccionService.registrarEgreso(any())).thenReturn(egresoResponse());

        mockMvc.perform(post(BASE_URL + "/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":20.00,\"descripcion\":\"Compra de insumos\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("EGRESO"));
    }

    @Test
    @DisplayName("POST /api/finanzas/egresos con MESERO → 403 Forbidden")
    @WithMockUser(roles = "MESERO")
    void registrarEgreso_conMesero_retorna403() throws Exception {
        mockMvc.perform(post(BASE_URL + "/egresos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":20.00,\"descripcion\":\"Compra de insumos\"}"))
                .andExpect(status().isForbidden());
    }

    // =====================================================================
    // GET /api/finanzas/transacciones
    // =====================================================================

    @Test
    @DisplayName("GET /api/finanzas/transacciones con ADMIN → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void listarTransacciones_conAdmin_retorna200() throws Exception {
        when(transaccionService.listarTransacciones(0, 20))
                .thenReturn(List.of(ingresoResponse(), egresoResponse()));

        mockMvc.perform(get(BASE_URL + "/transacciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/finanzas/transacciones con MESERO → 403 Forbidden")
    @WithMockUser(roles = "MESERO")
    void listarTransacciones_conMesero_retorna403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/transacciones"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/finanzas/transacciones?tipo=EGRESO con ADMIN → 200 OK")
    @WithMockUser(roles = "ADMIN")
    void listarTransaccionesPorTipo_conAdmin_retorna200() throws Exception {
        when(transaccionService.listarPorTipo(eq(TipoTransaccion.EGRESO)))
                .thenReturn(List.of(egresoResponse()));

        mockMvc.perform(get(BASE_URL + "/transacciones").param("tipo", "EGRESO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("EGRESO"));
    }

    // =====================================================================
    // GET /api/finanzas/resumen
    // =====================================================================

    @Test
    @DisplayName("GET /api/finanzas/resumen con ADMIN → 200 OK con balance correcto")
    @WithMockUser(roles = "ADMIN")
    void obtenerResumen_conAdmin_retorna200() throws Exception {
        final ResumenFinanciero resumen = new ResumenFinanciero(
                new BigDecimal("100.00"),
                new BigDecimal("30.00"),
                new BigDecimal("70.00"),
                ahora.minusDays(30),
                ahora
        );
        when(transaccionService.obtenerResumen(any(), any())).thenReturn(resumen);

        mockMvc.perform(get(BASE_URL + "/resumen")
                        .param("desde", "2026-01-01T00:00:00Z")
                        .param("hasta", "2026-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").value(100.00))
                .andExpect(jsonPath("$.totalEgresos").value(30.00))
                .andExpect(jsonPath("$.balance").value(70.00));
    }
}
