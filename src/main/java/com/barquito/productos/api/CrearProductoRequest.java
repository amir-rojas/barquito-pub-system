package com.barquito.productos.api;

import com.barquito.productos.domain.CategoriaProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada para la creación de un producto.
 *
 * @param nombre      nombre del producto; no puede ser blank.
 * @param precio      precio de venta; debe ser positivo.
 * @param descripcion descripción opcional del producto.
 * @param categoria   categoría del producto; no puede ser null.
 * @param disponible  si el producto estará disponible para pedidos desde el inicio.
 */
public record CrearProductoRequest(
        @NotBlank(message = "El nombre no puede estar vacío")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @Positive(message = "El precio debe ser positivo")
        BigDecimal precio,

        String descripcion,

        @NotNull(message = "La categoría es obligatoria")
        CategoriaProducto categoria,

        boolean disponible
) {}
