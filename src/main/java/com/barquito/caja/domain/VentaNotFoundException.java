package com.barquito.caja.domain;

/**
 * Excepción lanzada cuando no se encuentra una venta por su identificador.
 *
 * <p>Mapeada a HTTP 404 por {@code GlobalExceptionHandler}.
 */
public class VentaNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con el identificador de la venta no encontrada.
     *
     * @param id identificador de la venta buscada.
     */
    public VentaNotFoundException(final Long id) {
        super("Venta no encontrada: " + id);
    }
}
