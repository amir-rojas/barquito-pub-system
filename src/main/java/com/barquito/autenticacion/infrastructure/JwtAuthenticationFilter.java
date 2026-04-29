package com.barquito.autenticacion.infrastructure;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro de autenticación JWT que se ejecuta una sola vez por request.
 *
 * <p>Extrae el Bearer token del header {@code Authorization}, lo valida y,
 * si es válido, establece la autenticación en el {@link SecurityContextHolder}.
 *
 * <p>Los endpoints bajo {@code /api/auth/} se excluyen mediante
 * {@link #shouldNotFilter(HttpServletRequest)} para que el login no requiera token.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String PUBLIC_PATH_PREFIX = "/api/auth/";

    private final JwtService jwtService;

    /**
     * Construye el filtro con el servicio JWT.
     *
     * @param jwtService servicio para validar y parsear tokens.
     */
    public JwtAuthenticationFilter(final JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Excluye del filtro todos los paths que comienzan con {@code /api/auth/}.
     *
     * @param request el request HTTP.
     * @return {@code true} si el filtro NO debe aplicarse al request.
     */
    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        final String path = request.getServletPath();
        return path.startsWith(PUBLIC_PATH_PREFIX);
    }

    /**
     * Extrae el token JWT del header, lo valida y establece la autenticación.
     *
     * <p>Si el token es inválido o no está presente, la cadena de filtros continúa
     * sin autenticación y Spring Security devolverá 401 para endpoints protegidos.
     *
     * @param request     el request HTTP.
     * @param response    el response HTTP.
     * @param filterChain la cadena de filtros.
     */
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final String token = extraerBearerToken(request);

        if (StringUtils.hasText(token) && jwtService.esTokenValido(token)) {
            final String nombre = jwtService.extraerNombre(token);
            final String rol = jwtService.extraerRol(token);

            final var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
            final var authentication = new UsernamePasswordAuthenticationToken(
                    nombre, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Usuario autenticado: {} con rol: {}", nombre, rol);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token del header {@code Authorization} quitando el prefijo {@code Bearer }.
     *
     * @param request el request HTTP.
     * @return el token sin prefijo, o {@code null} si no hay header válido.
     */
    private String extraerBearerToken(final HttpServletRequest request) {
        final String header = request.getHeader(AUTH_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
