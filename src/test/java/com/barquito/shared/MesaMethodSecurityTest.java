package com.barquito.shared;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.shared.exception.GlobalExceptionHandler;
import com.barquito.shared.infrastructure.SecurityConfig;
import com.barquito.test.stubs.SecurityTripwireController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tripwire: verifica que {@code @EnableMethodSecurity} esté activo en {@link SecurityConfig}.
 *
 * <p>Un usuario con rol MESERO debe recibir 403 al intentar POST /api/test/mesas,
 * que está protegido con {@code @PreAuthorize("hasRole('ADMIN')")} en
 * {@link SecurityTripwireController}.
 *
 * <p>Si {@code @EnableMethodSecurity} no está activo, la anotación se ignora silenciosamente
 * y el test fallaría (recibiría 200 en lugar de 403).
 *
 * <p>El controlador stub usa el path {@code /api/test/mesas} para evitar conflictos
 * de mapping con el {@code MesaController} real cuando el contexto completo está activo.
 */
@WebMvcTest(SecurityTripwireController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MesaMethodSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    /**
     * MESERO intenta crear una mesa → debe recibir 403 Forbidden.
     *
     * <p>Tripwire: si {@code @EnableMethodSecurity} no está activo, este test FALLA
     * porque {@code @PreAuthorize} se ignora y el request pasa libremente (200).
     */
    @Test
    @DisplayName("SEC-01: MESERO→POST /api/test/mesas → 403 Forbidden (tripwire @EnableMethodSecurity)")
    @WithMockUser(roles = "MESERO")
    void mesero_postMesas_retorna403() throws Exception {
        mockMvc.perform(post("/api/test/mesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
