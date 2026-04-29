package com.barquito.autenticacion.infrastructure;

import com.barquito.autenticacion.application.TokenGeneratorPort;
import com.barquito.autenticacion.domain.Rol;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida que implementa {@link TokenGeneratorPort} usando JJWT.
 *
 * <p>Mantiene el acoplamiento con {@link JwtService} y {@link JwtProperties}
 * confinado a la capa de infraestructura.
 */
@Component
public class TokenGeneratorAdapter implements TokenGeneratorPort {

    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    /**
     * Construye el adaptador con sus dependencias JWT.
     *
     * @param jwtService    servicio de generación JWT.
     * @param jwtProperties propiedades de configuración JWT.
     */
    public TokenGeneratorAdapter(final JwtService jwtService, final JwtProperties jwtProperties) {
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String generarToken(final String nombre, final Rol rol) {
        return jwtService.generarToken(nombre, rol);
    }

    @Override
    public long getExpirationMs() {
        return jwtProperties.expirationMs();
    }
}
