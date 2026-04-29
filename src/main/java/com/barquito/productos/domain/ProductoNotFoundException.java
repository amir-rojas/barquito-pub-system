package com.barquito.productos.domain;

/**
 * Excepción de dominio lanzada cuando no se encuentra un producto por su id.
 *
 * <p>Se mapea a HTTP 404 Not Found en el {@code GlobalExceptionHandler}.
 */
public class ProductoNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message mensaje que describe el producto no encontrado.
     */
    public ProductoNotFoundException(final String message) {
        super(message);
    }
}
