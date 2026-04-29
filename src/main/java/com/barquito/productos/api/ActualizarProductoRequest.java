package com.barquito.productos.api;

import com.barquito.productos.domain.CategoriaProducto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO de entrada para la actualización de un producto existente.
 *
 * @param nombre      nuevo nombre del producto; no puede ser blank.
 * @param precio      nuevo precio de venta; debe ser positivo.
 * @param descripcion nueva descripción opcional del producto.
 * @param categoria   nueva categoría del producto; no puede ser null.
 * @param disponible  nuevo estado de disponibilidad del producto.
 */
public record ActualizarProductoRequest(
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
