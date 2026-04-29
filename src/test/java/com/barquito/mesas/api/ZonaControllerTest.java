package com.barquito.mesas.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.mesas.application.ZonaService;
import com.barquito.mesas.domain.Zona;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link ZonaController}.
 */
@WebMvcTest(ZonaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ZonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ZonaService zonaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/zonas";

    @Test
    @DisplayName("GET /api/zonas → 200 con lista vacía")
    @WithMockUser(roles = "ADMIN")
    void listar_autenticado_retorna200() throws Exception {
        when(zonaService.listarZonas()).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/zonas sin token → 401")
    void listar_sinToken_retorna401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/zonas con MESERO → 403")
    @WithMockUser(roles = "MESERO")
    void crear_mesero_retorna403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva\",\"orden\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/zonas con ADMIN → 201 Created")
    @WithMockUser(roles = "ADMIN")
    void crear_admin_retorna201() throws Exception {
        final Zona zona = new Zona(1L, "Nueva", null, 1);
        when(zonaService.crearZona(anyString(), any(), anyInt())).thenReturn(zona);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Nueva\",\"orden\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Nueva"));
    }

    @Test
    @DisplayName("GET /api/zonas con MESERO autenticado → 200")
    @WithMockUser(roles = "MESERO")
    void listar_mesero_retorna200() throws Exception {
        final Zona zona = new Zona(1L, "Salón", null, 0);
        when(zonaService.listarZonas()).thenReturn(List.of(zona));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Salón"));
    }
}
