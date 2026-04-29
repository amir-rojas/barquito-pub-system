package com.barquito.mesas.infrastructure;

import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.application.ZonaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.Mesa;
import com.barquito.mesas.domain.MesaOperacionInvalidaException;
import com.barquito.mesas.domain.Zona;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for mesa fusion/desfusion flow using Testcontainers.
 *
 * <p>Prueba el flujo completo: zona → 2 mesas → fusionar → verificar FUSIONADA
 * → desfusionar → verificar DISPONIBLE + casos de error (auto-fusión, encadenamiento).
 *
 * <p>Requiere Docker. Si Docker no está disponible, los tests se saltan automáticamente.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class MesaFusionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ZonaService zonaService;

    private Long zonaId;
    private Mesa mesaPrincipal;
    private Mesa mesaSecundaria;

    @BeforeEach
    void setUp() {
        final Zona zona = zonaService.crearZona(
                "TestZona-" + System.nanoTime(), "Zona de prueba", 99);
        zonaId = zona.id();
        mesaPrincipal = mesaService.crearMesa("P1-" + System.nanoTime(), zonaId, null);
        mesaSecundaria = mesaService.crearMesa("S1-" + System.nanoTime(), zonaId, null);
    }

    @Test
    @DisplayName("REQ-IT-01: flujo completo fusión → FUSIONADA → desfusión → DISPONIBLE")
    void fusionFlowCompleto_retornaEstadosCorrRectos() {
        // Fusionar
        final Mesa fusionada = mesaService.fusionarMesa(mesaPrincipal.id(), mesaSecundaria.id());
        assertThat(fusionada.estado()).isEqualTo(EstadoMesa.FUSIONADA);
        assertThat(fusionada.mesaPrincipalId()).isEqualTo(mesaPrincipal.id());

        // Verificar que está fusionada (consulta fresca)
        final Mesa fusionadaFresca = mesaService.listarMesasActivas().stream()
                .filter(m -> m.id().equals(mesaSecundaria.id()))
                .findFirst()
                .orElseThrow();
        assertThat(fusionadaFresca.estado()).isEqualTo(EstadoMesa.FUSIONADA);

        // Desfusionar
        final Mesa desfusionada = mesaService.desfusionarMesa(mesaSecundaria.id());
        assertThat(desfusionada.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
        assertThat(desfusionada.mesaPrincipalId()).isNull();
    }

    @Test
    @DisplayName("REQ-IT-02: auto-fusión (mesa consigo misma) → 409")
    void autoFusion_lanzaExcepcion409() {
        assertThatThrownBy(() ->
                mesaService.fusionarMesa(mesaPrincipal.id(), mesaPrincipal.id()))
                .isInstanceOf(MesaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("REQ-IT-03: encadenamiento (A→B y luego B→C donde C ya apunta a B) → 409")
    void encadenamiento_lanzaExcepcion409() {
        // Fusionar A (principal) con B (secundaria)
        mesaService.fusionarMesa(mesaPrincipal.id(), mesaSecundaria.id());

        // Intentar que B (ahora FUSIONADA) sea principal de otra mesa → debe fallar
        final Mesa mesaC = mesaService.crearMesa("C1-" + System.nanoTime(), zonaId, null);
        assertThatThrownBy(() ->
                mesaService.fusionarMesa(mesaSecundaria.id(), mesaC.id()))
                .isInstanceOf(MesaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("REQ-IT-04: desfusionar una mesa no fusionada → 409")
    void desfusionar_mesaNoFusionada_lanzaExcepcion() {
        assertThatThrownBy(() ->
                mesaService.desfusionarMesa(mesaPrincipal.id()))
                .isInstanceOf(MesaOperacionInvalidaException.class);
    }
}
