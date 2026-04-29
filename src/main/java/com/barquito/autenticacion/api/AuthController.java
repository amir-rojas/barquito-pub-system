package com.barquito.autenticacion.api;

import com.barquito.autenticacion.application.LoginResult;
import com.barquito.autenticacion.application.LoginUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Controlador REST para el módulo de autenticación.
 *
 * <p>Expone el endpoint de login bajo {@code /api/auth}.
 * Este path está configurado como público en {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    /**
     * Construye el controlador con el caso de uso de login.
     *
     * @param loginUseCase caso de uso de autenticación.
     */
    public AuthController(final LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    /**
     * Autentica a un usuario y retorna un JWT.
     *
     * @param request cuerpo con nombre y PIN del usuario; validado con {@code @Valid}.
     * @return {@code 200 OK} con el {@link TokenResponse}, o {@code 401} si las credenciales son inválidas.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody final LoginRequest request) {
        final LoginResult result = loginUseCase.login(request.nombre(), request.pin());
        final String expiresAt = Instant.now().plusMillis(result.expirationMs()).toString();
        return ResponseEntity.ok(new TokenResponse(
                result.token(),
                expiresAt,
                new TokenResponse.UsuarioResponse(result.id(), result.nombre(), result.rol())
        ));
    }
}
