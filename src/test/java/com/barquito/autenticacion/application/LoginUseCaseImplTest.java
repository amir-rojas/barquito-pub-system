package com.barquito.autenticacion.application;

import com.barquito.autenticacion.domain.CredencialesInvalidasException;
import com.barquito.autenticacion.domain.Rol;
import com.barquito.autenticacion.domain.Usuario;
import com.barquito.autenticacion.domain.UsuarioRepository;
import com.barquito.autenticacion.application.TokenGeneratorPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LoginUseCaseImpl}.
 */
@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGeneratorPort tokenGeneratorPort;

    @InjectMocks
    private LoginUseCaseImpl loginUseCase;

    private static final String NOMBRE = "admin";
    private static final String PIN = "1234";
    private static final String HASH = "$2a$10$hash";
    private static final String TOKEN = "eyJ.token.here";
    private static final long EXPIRATION_MS = 28_800_000L;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(1L, NOMBRE, HASH, Rol.ADMIN, true);
    }

    @Test
    @DisplayName("SC-01: login exitoso devuelve TokenResponse con datos correctos")
    void login_whenCredencialesValidas_retornaTokenResponse() {
        when(usuarioRepository.findByNombre(NOMBRE)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(PIN, HASH)).thenReturn(true);
        when(tokenGeneratorPort.generarToken(NOMBRE, Rol.ADMIN)).thenReturn(TOKEN);
        when(tokenGeneratorPort.getExpirationMs()).thenReturn(EXPIRATION_MS);

        LoginResult response = loginUseCase.login(NOMBRE, PIN);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(TOKEN);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.rol()).isEqualTo("ADMIN");
        assertThat(response.nombre()).isEqualTo(NOMBRE);
        assertThat(response.expirationMs()).isEqualTo(EXPIRATION_MS);
    }

    @Test
    @DisplayName("SC-03: PIN incorrecto lanza CredencialesInvalidasException con mensaje genérico")
    void login_whenPinIncorrecto_lanzaCredencialesInvalidasException() {
        when(usuarioRepository.findByNombre(NOMBRE)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(PIN, HASH)).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.login(NOMBRE, PIN))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    @DisplayName("SC-04: usuario inexistente lanza CredencialesInvalidasException con MISMO mensaje (anti-enumeration)")
    void login_whenUsuarioInexistente_lanzaMismoMensajeQueCredencialesInvalidas() {
        when(usuarioRepository.findByNombre("noexiste")).thenReturn(Optional.empty());
        // dummy check must still run — encoder must be called once
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> loginUseCase.login("noexiste", PIN))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas");

        // Verify dummy BCrypt was invoked (anti-timing-attack)
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }
}
