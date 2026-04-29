package com.barquito.autenticacion.infrastructure;

import com.barquito.autenticacion.domain.Rol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtService}.
 */
class JwtServiceTest {

    // 256-bit base64 secret for tests
    private static final String SECRET =
            "dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=";
    private static final long EXPIRATION_MS = 28_800_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET, EXPIRATION_MS);
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("SC-07: token generado es válido y puede ser verificado")
    void generarToken_tokenEsValido() {
        String token = jwtService.generarToken("admin", Rol.ADMIN);

        assertThat(token).isNotBlank();
        assertThat(jwtService.esTokenValido(token)).isTrue();
        assertThat(jwtService.extraerNombre(token)).isEqualTo("admin");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("SC-08: token expirado es inválido")
    void esTokenValido_tokenExpirado_retornaFalso() {
        JwtProperties expiradoProps = new JwtProperties(SECRET, -1L);
        JwtService jwtServiceExpirado = new JwtService(expiradoProps);
        String token = jwtServiceExpirado.generarToken("admin", Rol.ADMIN);

        assertThat(jwtService.esTokenValido(token)).isFalse();
    }

    @Test
    @DisplayName("SC-09: token adulterado es inválido")
    void esTokenValido_tokenAdulterado_retornaFalso() {
        String token = jwtService.generarToken("admin", Rol.ADMIN);
        String adulterado = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.esTokenValido(adulterado)).isFalse();
    }
}
