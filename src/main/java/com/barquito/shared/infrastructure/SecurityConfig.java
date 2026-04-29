package com.barquito.shared.infrastructure;

import com.barquito.autenticacion.infrastructure.JwtAuthenticationFilter;
import com.barquito.autenticacion.infrastructure.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de seguridad de la aplicación.
 *
 * <p>Sin {@code @Profile}: esta config es activa en todos los entornos.
 * El bug del scaffold original era el uso de {@code @Profile({"dev","test"})}
 * que dejaba producción desprotegida.
 *
 * <p>Reglas de autorización:
 * <ul>
 *   <li>{@code /api/auth/**} → público (login no requiere token)</li>
 *   <li>{@code /actuator/health} → público (health check para infra)</li>
 *   <li>Todo lo demás → autenticado</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * Construye la configuración con el servicio JWT necesario para el filtro.
     *
     * @param jwtService servicio de validación JWT.
     */
    public SecurityConfig(final JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Configura la cadena de filtros de seguridad.
     *
     * @param http el builder de HttpSecurity.
     * @return la cadena de filtros configurada.
     * @throws Exception si hay error en la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                        "No autorizado")));

        return http.build();
    }

    /**
     * Bean del filtro de autenticación JWT.
     *
     * @return instancia del filtro JWT.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * Bean del codificador de contraseñas con cost factor 10.
     *
     * @return codificador BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Bean del AuthenticationManager requerido por Spring Security.
     *
     * @param config la configuración de autenticación de Spring.
     * @return el AuthenticationManager.
     * @throws Exception si hay error al obtenerlo.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Configura CORS permitiendo la SPA frontend.
     *
     * <p>El origen se configura vía {@code app.cors.allowed-origins}
     * (default {@code http://localhost:3000} para desarrollo local).
     *
     * @return fuente de configuración CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
