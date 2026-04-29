package com.barquito.pedidos.domain;

/**
 * Excepción lanzada cuando se solicita un pedido que no existe.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en HTTP 404.
 */
public class PedidoNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con el id del pedido no encontrado.
     *
     * @param id identificador del pedido que no existe.
     */
    public PedidoNotFoundException(final Long id) {
        super("Pedido no encontrado: " + id);
    }
}
