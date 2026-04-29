package com.barquito.productos.application;

import com.barquito.productos.domain.CategoriaProducto;

import java.math.BigDecimal;

/**
 * Comando para crear un nuevo producto en el catálogo.
 *
 * @param nombre      nombre del producto (único, case-insensitive).
 * @param precio      precio de venta al público.
 * @param descripcion descripción opcional del producto.
 * @param categoria   categoría del producto.
 * @param disponible  si el producto estará disponible para pedidos desde el inicio.
 */
public record CrearProductoCommand(
        String nombre,
        BigDecimal precio,
        String descripcion,
        CategoriaProducto categoria,
        boolean disponible
) {}
