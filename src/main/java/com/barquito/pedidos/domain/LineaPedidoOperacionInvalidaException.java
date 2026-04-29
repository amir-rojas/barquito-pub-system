package com.barquito.pedidos.domain;

/**
 * Excepción lanzada cuando se intenta una operación inválida sobre una línea de pedido.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en HTTP 409 Conflict.
 */
public class LineaPedidoOperacionInvalidaException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message descripción de la operación inválida.
     */
    public LineaPedidoOperacionInvalidaException(final String message) {
        super(message);
    }
}
