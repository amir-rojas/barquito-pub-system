package com.barquito.productos.application;

import com.barquito.productos.domain.CategoriaProducto;

import java.math.BigDecimal;

/**
 * Comando para actualizar un producto existente en el catálogo.
 *
 * @param nombre      nuevo nombre del producto.
 * @param precio      nuevo precio de venta al público.
 * @param descripcion nueva descripción opcional del producto.
 * @param categoria   nueva categoría del producto.
 * @param disponible  nuevo estado de disponibilidad del producto.
 */
public record ActualizarProductoCommand(
        String nombre,
        BigDecimal precio,
        String descripcion,
        CategoriaProducto categoria,
        boolean disponible
) {}
