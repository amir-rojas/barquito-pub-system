package com.barquito.autenticacion.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el comportamiento de JwtProperties ante secret presente y ausente.
 */
@SpringBootTest(
        classes = JwtPropertiesTest.TestConfig.class,
        properties = {
                "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
                "jwt.expiration-ms=28800000"
        }
)
class JwtPropertiesTest {

    @EnableConfigurationProperties(JwtProperties.class)
    static class TestConfig {}

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    @DisplayName("SC-12 camino feliz: JwtProperties carga correctamente cuando JWT_SECRET está configurado")
    void jwtProperties_cuandoSecretConfigurado_cargaCorrectamente() {
        assertThat(jwtProperties.secret()).isNotBlank();
        assertThat(jwtProperties.expirationMs()).isPositive();
    }

    @Test
    @DisplayName("SC-12 negativo: el contexto falla al arrancar cuando jwt.secret está en blanco")
    void jwtProperties_cuandoSecretEnBlanco_fallaAlArrancar() {
        new ApplicationContextRunner()
                .withUserConfiguration(TestConfig.class)
                .withPropertyValues("jwt.secret=", "jwt.expiration-ms=28800000")
                .run(context -> assertThat(context).hasFailed());
    }
}
