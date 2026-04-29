package com.barquito.pedidos.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.pedidos.application.PedidoConLineas;
import com.barquito.pedidos.application.PedidoService;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.Pedido;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link PedidoController}.
 */
@WebMvcTest(PedidoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PedidoService pedidoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/pedidos";
    private final OffsetDateTime ahora = OffsetDateTime.now();

    private Pedido pedidoAbierto() {
        return new Pedido(1L, 10L, 5L, EstadoPedido.ABIERTO, null, ahora, ahora, null);
    }

    @Test
    @DisplayName("POST /api/pedidos sin token → 401")
    void crear_sinToken_retorna401() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mesaId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/pedidos con MESERO → 201")
    @WithMockUser(roles = "MESERO")
    void crear_conMesero_retorna201() throws Exception {
        when(pedidoService.crearPedido(anyLong(), anyString(), any()))
                .thenReturn(pedidoAbierto());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mesaId\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTO"));
    }

    @Test
    @DisplayName("POST /api/pedidos con ADMIN → 201")
    @WithMockUser(roles = "ADMIN")
    void crear_conAdmin_retorna201() throws Exception {
        when(pedidoService.crearPedido(anyLong(), anyString(), any()))
                .thenReturn(pedidoAbierto());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mesaId\":10}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/pedidos con MESERO → 200")
    @WithMockUser(roles = "MESERO")
    void listar_conMesero_retorna200() throws Exception {
        when(pedidoService.listarPedidosAbiertosByMesa(10L))
                .thenReturn(List.of(pedidoAbierto()));

        mockMvc.perform(get(BASE_URL).param("mesaId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} con ADMIN → 200")
    @WithMockUser(roles = "ADMIN")
    void buscarPorId_conAdmin_retorna200() throws Exception {
        when(pedidoService.buscarPedidoConLineas(1L))
                .thenReturn(new PedidoConLineas(pedidoAbierto(), List.of()));

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ABIERTO"));
    }

    @Test
    @DisplayName("POST /api/pedidos con mesaId nulo → 400")
    @WithMockUser(roles = "MESERO")
    void crear_sinMesaId_retorna400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pedidos/{id}/cerrar con MESERO → 200")
    @WithMockUser(roles = "MESERO")
    void cerrar_conMesero_retorna200() throws Exception {
        final Pedido cerrado = new Pedido(1L, 10L, 5L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);
        when(pedidoService.cerrarPedido(1L)).thenReturn(cerrado);

        mockMvc.perform(post(BASE_URL + "/1/cerrar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADO"));
    }
}
