package com.barquito.autenticacion.infrastructure;

import com.barquito.autenticacion.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Servicio para generar y validar JSON Web Tokens usando JJWT 0.12.x.
 *
 * <p><b>API usada (0.12.x — NO usar la de 0.11.x):</b>
 * <ul>
 *   <li>{@code Jwts.builder()} para generar</li>
 *   <li>{@code Jwts.parser().verifyWith(key).build().parseSignedClaims(token)} para parsear</li>
 *   <li>{@code Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))} para la clave</li>
 * </ul>
 */
@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private static final String CLAIM_ROL = "rol";

    private final SecretKey secretKey;
    private final long expirationMs;

    /**
     * Construye el servicio derivando la clave HMAC-SHA del secreto base64.
     *
     * @param jwtProperties propiedades con el secreto y la expiración.
     */
    public JwtService(final JwtProperties jwtProperties) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.secret()));
        this.expirationMs = jwtProperties.expirationMs();
    }

    /**
     * Genera un JWT firmado para el usuario dado.
     *
     * @param nombre nombre de login del usuario (subject del token).
     * @param rol    rol del usuario; se incluye como claim {@code "rol"}.
     * @return JWT compacto firmado.
     */
    public String generarToken(final String nombre, final Rol rol) {
        final Date ahora = new Date();
        final Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(nombre)
                .claim(CLAIM_ROL, rol.name())
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Verifica si un token JWT es válido (firma correcta y no expirado).
     *
     * @param token JWT compacto a verificar.
     * @return {@code true} si el token es válido; {@code false} en caso contrario.
     */
    public boolean esTokenValido(final String token) {
        try {
            parsearClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el nombre de usuario (subject) del token.
     *
     * @param token JWT válido.
     * @return nombre de usuario.
     */
    public String extraerNombre(final String token) {
        return parsearClaims(token).getSubject();
    }

    /**
     * Extrae el rol del token.
     *
     * @param token JWT válido.
     * @return valor del claim {@code "rol"}.
     */
    public String extraerRol(final String token) {
        return parsearClaims(token).get(CLAIM_ROL, String.class);
    }

    /**
     * Parsea y verifica el token retornando los claims.
     *
     * @param token JWT compacto.
     * @return {@link Claims} del payload.
     * @throws JwtException si el token es inválido o expirado.
     */
    private Claims parsearClaims(final String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
