package com.barquito;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Punto de entrada de la aplicación Barquito Backend.
 */
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class BackendApplication {

	/**
	 * Arranca la aplicación Spring Boot.
	 *
	 * @param args argumentos de línea de comandos.
	 */
	public static void main(final String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}
}
