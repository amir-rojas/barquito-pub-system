package com.barquito.productos.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad de dominio que representa un producto del catálogo del bar.
 *
 * <p>Inmutable por diseño: al ser un record no tiene setters ni estado mutable.
 * Todas las transiciones producen una nueva instancia mediante los métodos de dominio.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * @param id          identificador único del producto.
 * @param nombre      nombre del producto, único en el catálogo (case-insensitive).
 * @param precio      precio de venta al público.
 * @param descripcion descripción opcional del producto.
 * @param categoria   categoría del producto (CERVEZA, ESPIRITUOSO, GASEOSA, OTRO).
 * @param disponible  indica si el producto está disponible para pedidos en el momento.
 * @param activo      indica si el producto está activo en el catálogo (soft delete).
 * @param creadoEn    timestamp de creación del registro.
 */
public record Producto(
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
     * Produce una nueva instancia de este producto con {@code activo = false}.
     *
     * <p>Implementa soft delete: el producto se desactiva pero no se elimina de la BD.
     *
     * @return nuevo {@link Producto} con {@code activo} en {@code false}.
     */
    public Producto desactivar() {
        return new Producto(id, nombre, precio, descripcion, categoria, disponible, false, creadoEn);
    }

    /**
     * Produce una nueva instancia de este producto con los campos actualizados.
     *
     * <p>Preserva {@code id}, {@code activo} y {@code creadoEn} del original.
     *
     * @param nuevoNombre      nuevo nombre del producto.
     * @param nuevoPrecio      nuevo precio de venta.
     * @param nuevaDescripcion nueva descripción (puede ser {@code null}).
     * @param nuevaCategoria   nueva categoría del producto.
     * @param nuevoDisponible  nuevo estado de disponibilidad.
     * @return nuevo {@link Producto} con los valores actualizados.
     */
    public Producto actualizar(
            final String nuevoNombre,
            final BigDecimal nuevoPrecio,
            final String nuevaDescripcion,
            final CategoriaProducto nuevaCategoria,
            final boolean nuevoDisponible
    ) {
        return new Producto(
                id,
                nuevoNombre,
                nuevoPrecio,
                nuevaDescripcion,
                nuevaCategoria,
                nuevoDisponible,
                activo,
                creadoEn
        );
    }
}
