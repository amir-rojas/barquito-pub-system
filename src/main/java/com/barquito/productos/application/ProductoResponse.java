package com.barquito.productos.application;

import com.barquito.productos.domain.CategoriaProducto;
import com.barquito.productos.domain.Producto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO de respuesta que representa un producto del catálogo.
 *
 * @param id          identificador único del producto.
 * @param nombre      nombre del producto.
 * @param precio      precio de venta al público.
 * @param descripcion descripción opcional del producto.
 * @param categoria   categoría del producto.
 * @param disponible  indica si el producto está disponible para pedidos.
 * @param activo      indica si el producto está activo en el catálogo.
 * @param creadoEn    timestamp de creación del registro.
 */
public record ProductoResponse(
        Long id,
        String nombre,
        BigDecimal precio,
        String descripcion,
        CategoriaProducto categoria,
        boolean disponible,
        boolean activo,
        OffsetDateTime creadoEn
) {

    /**
     * Construye un {@link ProductoResponse} a partir de un {@link Producto} de dominio.
     *
     * @param producto el producto de dominio a convertir.
     * @return el DTO de respuesta correspondiente.
     */
    public static ProductoResponse from(final Producto producto) {
        return new ProductoResponse(
                producto.id(),
                producto.nombre(),
                producto.precio(),
                producto.descripcion(),
                producto.categoria(),
                producto.disponible(),
                producto.activo(),
                producto.creadoEn()
        );
    }
}
