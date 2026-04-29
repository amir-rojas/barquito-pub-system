package com.barquito.caja.domain;

/**
 * Excepción lanzada cuando se intenta una operación inválida sobre una venta.
 *
 * <p>Ejemplos: cobrar una venta ya PAGADA; anular una venta PAGADA; crear venta
 * de un pedido no CERRADO; pedido sin líneas facturables; mesa no en CUENTA_PEDIDA.
 *
 * <p>Mapeada a HTTP 409 por {@code GlobalExceptionHandler}.
 */
public class VentaOperacionInvalidaException extends RuntimeException {

    /**
     * Construye la excepción con el mensaje descriptivo de la operación rechazada.
     *
     * @param message descripción de la operación inválida.
     */
    public VentaOperacionInvalidaException(final String message) {
        super(message);
    }
}
