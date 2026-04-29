package com.barquito.mesas.domain;

/**
 * Excepción lanzada cuando se solicita una mesa que no existe en el sistema.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en una
 * respuesta HTTP 404 Not Found.
 */
public class MesaNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con el id de la mesa no encontrada.
     *
     * @param id identificador de la mesa que no existe.
     */
    public MesaNotFoundException(final Long id) {
        super("Mesa no encontrada: " + id);
    }
}
