package com.barquito.mesas.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice tests for {@link ZonaJpaRepository} using {@code @DataJpaTest} with Testcontainers.
 *
 * <p>Usa PostgreSQL real mediante Testcontainers + {@code @ServiceConnection} para auto-configurar
 * el datasource. Flyway aplica las migraciones automáticamente incluyendo V3.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
class ZonaJpaRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ZonaJpaRepository zonaJpaRepository;

    @Test
    @DisplayName("save persiste una zona y le asigna id")
    void save_zonaValida_persisteConId() {
        final ZonaEntity zona = new ZonaEntity(null, "Terraza", "Exterior", 1);

        final ZonaEntity guardada = zonaJpaRepository.save(zona);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getNombre()).isEqualTo("Terraza");
    }

    @Test
    @DisplayName("findByNombreIgnoreCase retorna zona para nombre en minúsculas")
    void findByNombreIgnoreCase_nombreMinusculas_retornaZona() {
        zonaJpaRepository.save(new ZonaEntity(null, "Barra", null, 0));

        final Optional<ZonaEntity> result = zonaJpaRepository.findByNombreIgnoreCase("barra");

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Barra");
    }

    @Test
    @DisplayName("existsByNombreIgnoreCase retorna true para zona existente")
    void existsByNombreIgnoreCase_zonaExistente_retornaTrue() {
        zonaJpaRepository.save(new ZonaEntity(null, "VIP", null, 2));

        assertThat(zonaJpaRepository.existsByNombreIgnoreCase("vip")).isTrue();
    }

    @Test
    @DisplayName("findAllByOrderByOrdenAsc retorna zonas ordenadas por orden")
    void findAllByOrderByOrdenAsc_retornaOrdenadas() {
        zonaJpaRepository.save(new ZonaEntity(null, "Z-Alta", null, 3));
        zonaJpaRepository.save(new ZonaEntity(null, "A-Baja", null, 1));

        final List<ZonaEntity> result = zonaJpaRepository.findAllByOrderByOrdenAsc();

        // Incluye la zona semilla de V3 (orden=0) + las dos anteriores
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
        // Verificar que las zonas guardadas están en orden correcto entre sí
        final int idxA = result.indexOf(result.stream()
                .filter(z -> "A-Baja".equals(z.getNombre())).findFirst().orElseThrow());
        final int idxZ = result.indexOf(result.stream()
                .filter(z -> "Z-Alta".equals(z.getNombre())).findFirst().orElseThrow());
        assertThat(idxA).isLessThan(idxZ);
    }
}
