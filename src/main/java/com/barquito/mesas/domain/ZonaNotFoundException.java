package com.barquito.mesas.domain;

/**
 * Excepción lanzada cuando se solicita una zona que no existe en el sistema.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en una
 * respuesta HTTP 404 Not Found.
 */
public class ZonaNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con el id de la zona no encontrada.
     *
     * @param id identificador de la zona que no existe.
     */
    public ZonaNotFoundException(final Long id) {
        super("Zona no encontrada: " + id);
    }
}
