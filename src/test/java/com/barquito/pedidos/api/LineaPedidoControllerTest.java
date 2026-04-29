package com.barquito.pedidos.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.pedidos.application.LineaPedidoService;
import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.LineaPedido;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link LineaPedidoController}.
 */
@WebMvcTest(LineaPedidoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class LineaPedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LineaPedidoService lineaPedidoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/pedidos/1/lineas";
    private final OffsetDateTime ahora = OffsetDateTime.now();

    private LineaPedido lineaPendiente() {
        return new LineaPedido(1L, 1L, 100L,
                new BigDecimal("2.000"), new BigDecimal("10.00"), new BigDecimal("20.00"),
                EstadoLinea.PENDIENTE, null, ahora, ahora);
    }

    @Test
    @DisplayName("POST /api/pedidos/1/lineas sin token → 401")
    void agregar_sinToken_retorna401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":100,\"cantidad\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/pedidos/1/lineas con MESERO → 201")
    @WithMockUser(roles = "MESERO")
    void agregar_conMesero_retorna201() throws Exception {
        when(lineaPedidoService.agregarLinea(anyLong(), anyLong(), any(), any()))
                .thenReturn(lineaPendiente());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\":100,\"cantidad\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("GET /api/pedidos/1/lineas con MESERO → 200")
    @WithMockUser(roles = "MESERO")
    void listar_conMesero_retorna200() throws Exception {
        when(lineaPedidoService.listarLineas(1L)).thenReturn(List.of(lineaPendiente()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("PATCH /api/pedidos/1/lineas/1/estado con MESERO → 200")
    @WithMockUser(roles = "MESERO")
    void cambiarEstado_conMesero_retorna200() throws Exception {
        final LineaPedido enPreparacion = new LineaPedido(1L, 1L, 100L,
                new BigDecimal("2.000"), new BigDecimal("10.00"), new BigDecimal("20.00"),
                EstadoLinea.EN_PREPARACION, null, ahora, ahora);
        when(lineaPedidoService.cambiarEstadoLinea(anyLong(), any(), anyString()))
                .thenReturn(enPreparacion);

        mockMvc.perform(patch(BASE_URL + "/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"EN_PREPARACION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"));
    }

    @Test
    @DisplayName("DELETE /api/pedidos/1/lineas/1 con MESERO → 204")
    @WithMockUser(roles = "MESERO")
    void cancelar_conMesero_retorna204() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST lineas con productoId nulo → 400")
    @WithMockUser(roles = "MESERO")
    void agregar_sinProductoId_retorna400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cantidad\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH estado con enum inválido → 400")
    @WithMockUser(roles = "MESERO")
    void cambiarEstado_enumInvalido_retorna400() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"BANANA\"}"))
                .andExpect(status().isBadRequest());
    }
}
