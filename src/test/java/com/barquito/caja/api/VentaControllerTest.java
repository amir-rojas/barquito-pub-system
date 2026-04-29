package com.barquito.caja.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.caja.application.VentaConDetalles;
import com.barquito.caja.application.VentaService;
import com.barquito.caja.domain.DetalleVenta;
import com.barquito.caja.domain.EstadoVenta;
import com.barquito.caja.domain.MetodoPago;
import com.barquito.caja.domain.Venta;
import com.barquito.caja.domain.VentaNotFoundException;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link VentaController}.
 */
@WebMvcTest(VentaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VentaService ventaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/caja/ventas";
    private final OffsetDateTime ahora = OffsetDateTime.now();

    private VentaConDetalles ventaConDetallesPendiente() {
        final Venta venta = new Venta(1L, 10L, 3L, 5L,
                new BigDecimal("22.50"), null, EstadoVenta.PENDIENTE, ahora, null, null);
        final DetalleVenta detalle = new DetalleVenta(1L, 1L, 100L, "Birra",
                new BigDecimal("2.000"), new BigDecimal("5.00"), new BigDecimal("10.00"));
        return new VentaConDetalles(venta, List.of(detalle));
    }

    private VentaConDetalles ventaConDetallesPagada() {
        final Venta venta = new Venta(1L, 10L, 3L, 5L,
                new BigDecimal("22.50"), MetodoPago.EFECTIVO, EstadoVenta.PAGADA, ahora, ahora, null);
        return new VentaConDetalles(venta, List.of());
    }

    private VentaConDetalles ventaConDetallesAnulada() {
        final Venta venta = new Venta(1L, 10L, 3L, 5L,
                new BigDecimal("22.50"), null, EstadoVenta.ANULADA, ahora, null, ahora);
        return new VentaConDetalles(venta, List.of());
    }

    // =====================================================================
    // POST /api/caja/ventas — crearVenta
    // =====================================================================

    @Test
    @DisplayName("POST /api/caja/ventas con MESERO → 201")
    @WithMockUser(roles = "MESERO")
    void crear_conMesero_retorna201() throws Exception {
        when(ventaService.crearVenta(anyLong(), anyString()))
                .thenReturn(ventaConDetallesPendiente());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pedidoId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("POST /api/caja/ventas con ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void crear_conAdmin_retorna201() throws Exception {
        when(ventaService.crearVenta(anyLong(), anyString()))
                .thenReturn(ventaConDetallesPendiente());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pedidoId\":10}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/caja/ventas con pedidoId null → 400")
    @WithMockUser(roles = "MESERO")
    void crear_pedidoIdNull_retorna400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/caja/ventas sin autenticación → 401")
    void crear_sinToken_retorna401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pedidoId\":10}"))
                .andExpect(status().isUnauthorized());
    }

    // =====================================================================
    // POST /api/caja/ventas/{id}/cobrar
    // =====================================================================

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/cobrar con MESERO → 200")
    @WithMockUser(roles = "MESERO")
    void cobrar_conMesero_retorna200() throws Exception {
        when(ventaService.cobrarVenta(anyLong(), any(MetodoPago.class)))
                .thenReturn(ventaConDetallesPagada());

        mockMvc.perform(post(BASE_URL + "/1/cobrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metodoPago\":\"EFECTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PAGADA"));
    }

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/cobrar con metodoPago null → 400")
    @WithMockUser(roles = "MESERO")
    void cobrar_metodoPagoNull_retorna400() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/cobrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/cobrar → 404 cuando VentaNotFoundException")
    @WithMockUser(roles = "MESERO")
    void cobrar_ventaNoEncontrada_retorna404() throws Exception {
        when(ventaService.cobrarVenta(anyLong(), any(MetodoPago.class)))
                .thenThrow(new VentaNotFoundException(999L));

        mockMvc.perform(post(BASE_URL + "/999/cobrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metodoPago\":\"EFECTIVO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/cobrar → 409 cuando VentaOperacionInvalidaException")
    @WithMockUser(roles = "MESERO")
    void cobrar_ventaPagada_retorna409() throws Exception {
        when(ventaService.cobrarVenta(anyLong(), any(MetodoPago.class)))
                .thenThrow(new VentaOperacionInvalidaException("Venta ya PAGADA"));

        mockMvc.perform(post(BASE_URL + "/1/cobrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"metodoPago\":\"EFECTIVO\"}"))
                .andExpect(status().isConflict());
    }

    // =====================================================================
    // POST /api/caja/ventas/{id}/anular
    // =====================================================================

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/anular con ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void anular_conAdmin_retorna200() throws Exception {
        when(ventaService.anularVenta(anyLong()))
                .thenReturn(ventaConDetallesAnulada());

        mockMvc.perform(post(BASE_URL + "/1/anular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"));
    }

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/anular con MESERO → 403 (CRÍTICO: solo ADMIN puede anular)")
    @WithMockUser(roles = "MESERO")
    void anular_conMesero_retorna403() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/anular"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/anular → 404 cuando VentaNotFoundException")
    @WithMockUser(roles = "ADMIN")
    void anular_ventaNoEncontrada_retorna404() throws Exception {
        when(ventaService.anularVenta(anyLong()))
                .thenThrow(new VentaNotFoundException(999L));

        mockMvc.perform(post(BASE_URL + "/999/anular"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/caja/ventas/{id}/anular → 409 cuando venta PAGADA")
    @WithMockUser(roles = "ADMIN")
    void anular_ventaPagada_retorna409() throws Exception {
        when(ventaService.anularVenta(anyLong()))
                .thenThrow(new VentaOperacionInvalidaException("No se puede anular PAGADA"));

        mockMvc.perform(post(BASE_URL + "/1/anular"))
                .andExpect(status().isConflict());
    }

    // =====================================================================
    // GET /api/caja/ventas/{id}
    // =====================================================================

    @Test
    @DisplayName("GET /api/caja/ventas/{id} → 200")
    @WithMockUser(roles = "MESERO")
    void buscarPorId_retorna200() throws Exception {
        when(ventaService.buscarVenta(1L))
                .thenReturn(ventaConDetallesPendiente());

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/caja/ventas/{id} → 404 cuando VentaNotFoundException")
    @WithMockUser(roles = "MESERO")
    void buscarPorId_noEncontrada_retorna404() throws Exception {
        when(ventaService.buscarVenta(999L))
                .thenThrow(new VentaNotFoundException(999L));

        mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    // =====================================================================
    // GET /api/caja/ventas?pedidoId=X
    // =====================================================================

    @Test
    @DisplayName("GET /api/caja/ventas?pedidoId=X → 200")
    @WithMockUser(roles = "MESERO")
    void buscarPorPedido_retorna200() throws Exception {
        when(ventaService.buscarPorPedido(10L))
                .thenReturn(Optional.of(ventaConDetallesPendiente()));

        mockMvc.perform(get(BASE_URL).param("pedidoId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(10));
    }

    @Test
    @DisplayName("GET /api/caja/ventas?pedidoId=X → 404 cuando Optional.empty()")
    @WithMockUser(roles = "MESERO")
    void buscarPorPedido_noEncontrada_retorna404() throws Exception {
        when(ventaService.buscarPorPedido(999L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get(BASE_URL).param("pedidoId", "999"))
                .andExpect(status().isNotFound());
    }
}
