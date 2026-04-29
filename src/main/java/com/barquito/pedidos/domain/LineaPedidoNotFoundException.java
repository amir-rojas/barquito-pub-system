package com.barquito.pedidos.domain;

/**
 * Excepción lanzada cuando se solicita una línea de pedido que no existe.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en HTTP 404.
 */
public class LineaPedidoNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con el id de la línea no encontrada.
     *
     * @param id identificador de la línea que no existe.
     */
    public LineaPedidoNotFoundException(final Long id) {
        super("Línea de pedido no encontrada: " + id);
    }
}
