package com.barquito.mesas.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.FormaMesa;
import com.barquito.mesas.domain.Mesa;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link MesaController}.
 *
 * <p>Tests de seguridad (roles, token) y tests de lógica con mocks del servicio.
 */
@WebMvcTest(MesaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MesaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MesaService mesaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/mesas";

    private Mesa mesaDisponible() {
        return new Mesa(1L, "1", EstadoMesa.DISPONIBLE, true, 1L, FormaMesa.CIRCULAR, null);
    }

    @Test
    @DisplayName("GET /api/mesas sin token → 401")
    void listar_sinToken_retorna401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/mesas con usuario autenticado → 200")
    @WithMockUser(roles = "MESERO")
    void listar_autenticado_retorna200() throws Exception {
        when(mesaService.listarMesasActivas()).thenReturn(List.of(mesaDisponible()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("1"));
    }

    @Test
    @DisplayName("POST /api/mesas con MESERO → 403 (tripwire @EnableMethodSecurity)")
    @WithMockUser(roles = "MESERO")
    void crear_mesero_retorna403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":\"1\",\"zonaId\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/mesas con ADMIN → 201 Created")
    @WithMockUser(roles = "ADMIN")
    void crear_admin_retorna201() throws Exception {
        when(mesaService.crearMesa(anyString(), anyLong(), any())).thenReturn(mesaDisponible());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":\"1\",\"zonaId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("POST /api/mesas/{id}/fusionar con MESERO → 200 OK")
    @WithMockUser(roles = "MESERO")
    void fusionar_mesero_retorna200() throws Exception {
        final Mesa fusionada = new Mesa(2L, "2", EstadoMesa.FUSIONADA, true, 1L, null, 1L);
        when(mesaService.fusionarMesa(anyLong(), anyLong())).thenReturn(fusionada);

        mockMvc.perform(post(BASE_URL + "/1/fusionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"secundariaId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("FUSIONADA"));
    }

    @Test
    @DisplayName("POST /api/mesas/{id}/fusionar sin token → 401")
    void fusionar_sinToken_retorna401() throws Exception {
        mockMvc.perform(post(BASE_URL + "/1/fusionar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"secundariaId\":2}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/mesas/{id} con usuario autenticado → 200")
    @WithMockUser(roles = "MESERO")
    void buscarPorId_autenticado_retorna200() throws Exception {
        when(mesaService.buscarMesa(1L)).thenReturn(mesaDisponible());

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PATCH /api/mesas/{id}/estado con MESERO → 200 (permitido)")
    @WithMockUser(roles = "MESERO")
    void cambiarEstado_mesero_retorna200() throws Exception {
        when(mesaService.cambiarEstado(anyLong(), any())).thenReturn(mesaDisponible());

        mockMvc.perform(patch(BASE_URL + "/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\":\"DISPONIBLE\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/mesas/{id}/activa con MESERO → 403 (solo ADMIN)")
    @WithMockUser(roles = "MESERO")
    void cambiarActiva_mesero_retorna403() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/1/activa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activa\":true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/mesas/{id} con MESERO → 403 (solo ADMIN)")
    @WithMockUser(roles = "MESERO")
    void actualizar_mesero_retorna403() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numero\":\"1\",\"zonaId\":1}"))
                .andExpect(status().isForbidden());
    }
}
