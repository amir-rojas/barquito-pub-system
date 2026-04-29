package com.barquito.mesas.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice tests for {@link MesaJpaRepository} using {@code @DataJpaTest} with Testcontainers.
 *
 * <p>Usa PostgreSQL real mediante Testcontainers + {@code @ServiceConnection} para auto-configurar
 * el datasource. Flyway aplica las migraciones automáticamente incluyendo V3.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class MesaJpaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MesaJpaRepository mesaJpaRepository;

    @Autowired
    private ZonaJpaRepository zonaJpaRepository;

    private Long zonaId;

    @BeforeEach
    void setUp() {
        // La zona semilla de V3 (id=1) debería existir; si no, creamos una
        zonaId = zonaJpaRepository.findAll().stream()
                .findFirst()
                .map(ZonaEntity::getId)
                .orElseGet(() -> zonaJpaRepository.save(
                        new ZonaEntity(null, "Salón", null, 0)).getId());
    }

    private MesaEntity saveMesa(final String numero, final String estado, final boolean activa) {
        return mesaJpaRepository.save(
                new MesaEntity(null, numero, estado, activa, zonaId, null, null));
    }

    @Test
    @DisplayName("save persiste una mesa y le asigna id")
    void save_mesaValida_persisteConId() {
        final MesaEntity guardada = saveMesa("M1", "DISPONIBLE", true);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNumero()).isEqualTo("M1");
        assertThat(guardada.getEstado()).isEqualTo("DISPONIBLE");
    }

    @Test
    @DisplayName("findAllByActivaTrue retorna solo mesas activas")
    void findAllByActivaTrue_retoraSoloActivas() {
        saveMesa("M2", "DISPONIBLE", true);
        saveMesa("M3", "DISPONIBLE", false);

        final List<MesaEntity> activas = mesaJpaRepository.findAllByActivaTrue();

        assertThat(activas).allMatch(MesaEntity::isActiva);
        assertThat(activas).anyMatch(m -> "M2".equals(m.getNumero()));
        assertThat(activas).noneMatch(m -> "M3".equals(m.getNumero()));
    }

    @Test
    @DisplayName("findAllByZonaIdAndActivaTrue retorna mesas activas de la zona")
    void findAllByZonaIdAndActivaTrue_retornaMesasDeZona() {
        saveMesa("M4", "DISPONIBLE", true);

        final List<MesaEntity> result = mesaJpaRepository.findAllByZonaIdAndActivaTrue(zonaId);

        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(m -> zonaId.equals(m.getZonaId()));
    }

    @Test
    @DisplayName("findAllByMesaPrincipalIdAndActivaTrue retorna secundarias activas")
    void findAllByMesaPrincipalIdAndActivaTrue_retornaSecundariasActivas() {
        final MesaEntity principal = saveMesa("M5", "OCUPADA", true);
        final MesaEntity secundaria = mesaJpaRepository.save(
                new MesaEntity(null, "M6", "FUSIONADA", true, zonaId, null, principal.getId()));

        final List<MesaEntity> secundarias =
                mesaJpaRepository.findAllByMesaPrincipalIdAndActivaTrue(principal.getId());

        assertThat(secundarias).hasSize(1);
        assertThat(secundarias.get(0).getId()).isEqualTo(secundaria.getId());
    }
}
