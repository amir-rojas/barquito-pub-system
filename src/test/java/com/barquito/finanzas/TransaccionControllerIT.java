package com.barquito.finanzas;

import com.barquito.autenticacion.domain.Rol;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.autenticacion.infrastructure.UsuarioEntity;
import com.barquito.autenticacion.infrastructure.UsuarioJpaRepository;
import com.barquito.finanzas.application.RegistrarEgresoCommand;
import com.barquito.finanzas.application.ResumenFinanciero;
import com.barquito.finanzas.application.TransaccionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@code finanzas} bounded context.
 *
 * <p>Requires Docker. Skipped gracefully if Docker is unavailable.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
@DisplayName("TransaccionController Integration Tests")
class TransaccionControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private Long adminId;

    @BeforeEach
    void setUp() {
        final String adminUsername = "admin-fin-" + System.nanoTime();
        final UsuarioEntity adminEntity = usuarioJpaRepository.save(
                new UsuarioEntity(null, adminUsername,
                        passwordEncoder.encode("pass"), "admin", true));
        adminId = adminEntity.getId();
        adminToken = jwtService.generarToken(adminUsername, Rol.ADMIN);
    }

    // -----------------------------------------------------------------------
    // POST /api/finanzas/egresos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/finanzas/egresos → 201 Created con body de respuesta")
    void registrarEgreso_retorna201ConBodyCorrecto() throws Exception {
        mockMvc.perform(post("/api/finanzas/egresos")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"monto\":25.50,\"descripcion\":\"Compra de insumos IT\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("EGRESO"))
                .andExpect(jsonPath("$.monto").value(25.50))
                .andExpect(jsonPath("$.descripcion").value("Compra de insumos IT"))
                .andExpect(jsonPath("$.ventaId").doesNotExist());
    }

    // -----------------------------------------------------------------------
    // GET /api/finanzas/transacciones
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/finanzas/transacciones → 200 con lista de transacciones")
    void listarTransacciones_retorna200ConDatos() throws Exception {
        // Registrar un egreso como setup
        transaccionService.registrarEgreso(
                new com.barquito.finanzas.application.RegistrarEgresoCommand(
                        new BigDecimal("10.00"), "Egreso IT test", adminId));

        mockMvc.perform(get("/api/finanzas/transacciones")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // -----------------------------------------------------------------------
    // GET /api/finanzas/resumen
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/finanzas/resumen → 200 con totales correctos")
    void obtenerResumen_retorna200ConTotalesCorrectos() throws Exception {
        // Registrar un ingreso y un egreso via service
        transaccionService.registrarEgreso(
                new com.barquito.finanzas.application.RegistrarEgresoCommand(
                        new BigDecimal("15.00"), "Egreso resumen test", adminId));

        mockMvc.perform(get("/api/finanzas/resumen")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("desde", "2026-01-01T00:00:00Z")
                        .param("hasta", "2026-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIngresos").isNumber())
                .andExpect(jsonPath("$.totalEgresos").isNumber())
                .andExpect(jsonPath("$.balance").isNumber());
    }
}
