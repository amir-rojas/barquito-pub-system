package com.barquito.pedidos.domain;

/**
 * Excepción lanzada cuando se intenta una operación inválida sobre un pedido.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en HTTP 409 Conflict.
 */
public class PedidoOperacionInvalidaException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message descripción de la operación inválida.
     */
    public PedidoOperacionInvalidaException(final String message) {
        super(message);
    }
}
