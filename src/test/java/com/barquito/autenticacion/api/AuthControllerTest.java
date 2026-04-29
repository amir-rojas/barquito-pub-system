package com.barquito.autenticacion.api;

import com.barquito.autenticacion.application.LoginResult;
import com.barquito.autenticacion.application.LoginUseCase;
import com.barquito.autenticacion.domain.CredencialesInvalidasException;
import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.shared.exception.GlobalExceptionHandler;
import com.barquito.shared.infrastructure.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link AuthController}.
 *
 * <p>Importa {@link SecurityConfig} para probar reglas de autorización reales
 * (qué paths son públicos y cuáles requieren token).
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String TOKEN = "eyJ.test.token";

    @Test
    @DisplayName("SC-02: login es case-insensitive en el nombre")
    void login_nombreEnMayusculas_retorna200() throws Exception {
        final LoginResult result = new LoginResult(TOKEN, 1L, "ADMIN", "ADMIN", 28800000L);
        when(loginUseCase.login(eq("ADMIN"), anyString())).thenReturn(result);

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("ADMIN", "1234"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN))
                .andExpect(jsonPath("$.usuario.rol").value("ADMIN"))
                .andExpect(jsonPath("$.usuario.nombre").value("ADMIN"))
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    @DisplayName("SC-05: nombre o pin en blanco retorna 400 con detalle de errores")
    void login_nombrePinBlank_retorna400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"pin\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SC-06: body vacío retorna 400")
    void login_bodyVacio_retorna400() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("SC-10: endpoint protegido sin token retorna 401")
    void endpointProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/mesas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SC-11: endpoint /api/auth/login no requiere token")
    void loginEndpoint_esPublico_noRequiereToken() throws Exception {
        when(loginUseCase.login(anyString(), anyString()))
                .thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("x", "y"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
    }
}
