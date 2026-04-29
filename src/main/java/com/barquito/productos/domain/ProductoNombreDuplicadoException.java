package com.barquito.productos.domain;

/**
 * Excepción de dominio lanzada cuando se intenta crear o renombrar un producto
 * con un nombre que ya existe en el catálogo (case-insensitive).
 *
 * <p>Se mapea a HTTP 409 Conflict en el {@code GlobalExceptionHandler}.
 */
public class ProductoNombreDuplicadoException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo.
     *
     * @param message mensaje que describe el nombre duplicado.
     */
    public ProductoNombreDuplicadoException(final String message) {
        super(message);
    }
}
