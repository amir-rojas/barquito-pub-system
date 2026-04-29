package com.barquito.autenticacion.infrastructure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Propiedades de configuración JWT.
 *
 * <p>Se ligan al prefijo {@code jwt} en {@code application.yaml}.
 * Si {@code JWT_SECRET} no está definido en el entorno, el arranque falla
 * con un error de validación (SC-12).
 *
 * @param secret       secreto base64 de al menos 256 bits para firmar los JWT.
 * @param expirationMs tiempo de vida del token en milisegundos.
 */
@ConfigurationProperties(prefix = "jwt")
@Validated
public record JwtProperties(
        @NotBlank String secret,
        @Positive long expirationMs
) {}
