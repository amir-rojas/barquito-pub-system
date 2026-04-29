package com.barquito.pedidos.domain;

/**
 * Excepción lanzada cuando se solicita un producto que no existe o no está activo.
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en HTTP 404.
 */
public class ProductoNotFoundException extends RuntimeException {

    /**
     * Construye la excepción con el id del producto no encontrado.
     *
     * @param id identificador del producto que no existe o no está activo.
     */
    public ProductoNotFoundException(final Long id) {
        super("Producto no encontrado o inactivo: " + id);
    }
}
