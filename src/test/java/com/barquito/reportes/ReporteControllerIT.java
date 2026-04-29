package com.barquito.reportes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@code reportes} bounded context.
 *
 * <p>Requires Docker. Skipped gracefully if Docker is unavailable.
 * Verifies that all endpoints return correct HTTP status and valid JSON structure
 * when the database is empty (0 ventas).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class ReporteControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/reportes";
    private static final String DESDE = "2026-04-01T00:00:00+00:00";
    private static final String HASTA = "2026-04-30T23:59:59+00:00";

    // =========================================================================
    // ventas-diarias
    // =========================================================================

    @Test
    @DisplayName("GET /ventas-diarias con ADMIN y BD vacía → 200 OK con totalVentas=0")
    @WithMockUser(roles = "ADMIN")
    void ventasDiarias_admin_bdVacia_retorna200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/ventas-diarias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentas").value(0))
                .andExpect(jsonPath("$.montoTotal").exists())
                .andExpect(jsonPath("$.montoEfectivo").exists())
                .andExpect(jsonPath("$.montoQr").exists());
    }

    @Test
    @DisplayName("GET /ventas-diarias con MESERO → 403 Forbidden")
    @WithMockUser(roles = "MESERO")
    void ventasDiarias_mesero_retorna403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/ventas-diarias"))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // top-productos
    // =========================================================================

    @Test
    @DisplayName("GET /top-productos con ADMIN y BD vacía → 200 OK con lista vacía")
    @WithMockUser(roles = "ADMIN")
    void topProductos_admin_bdVacia_retorna200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/top-productos")
                        .param("desde", DESDE)
                        .param("hasta", HASTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /top-productos con MESERO → 403 Forbidden")
    @WithMockUser(roles = "MESERO")
    void topProductos_mesero_retorna403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/top-productos")
                        .param("desde", DESDE)
                        .param("hasta", HASTA))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // ventas-por-categoria
    // =========================================================================

    @Test
    @DisplayName("GET /ventas-por-categoria con ADMIN y BD vacía → 200 OK con lista vacía")
    @WithMockUser(roles = "ADMIN")
    void ventasPorCategoria_admin_bdVacia_retorna200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/ventas-por-categoria")
                        .param("desde", DESDE)
                        .param("hasta", HASTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /ventas-por-categoria con MESERO → 403 Forbidden")
    @WithMockUser(roles = "MESERO")
    void ventasPorCategoria_mesero_retorna403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/ventas-por-categoria")
                        .param("desde", DESDE)
                        .param("hasta", HASTA))
                .andExpect(status().isForbidden());
    }

    // =========================================================================
    // resumen
    // =========================================================================

    @Test
    @DisplayName("GET /resumen con ADMIN y BD vacía → 200 OK con todos los campos")
    @WithMockUser(roles = "ADMIN")
    void resumen_admin_bdVacia_retorna200() throws Exception {
        mockMvc.perform(get(BASE_URL + "/resumen")
                        .param("desde", DESDE)
                        .param("hasta", HASTA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVentas").exists())
                .andExpect(jsonPath("$.montoVentas").exists())
                .andExpect(jsonPath("$.ventasAnuladas").exists())
                .andExpect(jsonPath("$.totalIngresos").exists())
                .andExpect(jsonPath("$.totalEgresos").exists())
                .andExpect(jsonPath("$.balance").exists());
    }

    @Test
    @DisplayName("GET /resumen con MESERO → 403 Forbidden")
    @WithMockUser(roles = "MESERO")
    void resumen_mesero_retorna403() throws Exception {
        mockMvc.perform(get(BASE_URL + "/resumen")
                        .param("desde", DESDE)
                        .param("hasta", HASTA))
                .andExpect(status().isForbidden());
    }
}
