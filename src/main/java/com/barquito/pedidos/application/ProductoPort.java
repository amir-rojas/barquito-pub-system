package com.barquito.pedidos.application;

import java.util.Optional;

/**
 * Puerto de salida para consultar datos de productos desde el dominio de pedidos.
 *
 * <p>Desacopla el módulo de pedidos del módulo de inventario. Solo expone
 * los datos mínimos necesarios para crear líneas (precio snapshot + estado activo).
 */
public interface ProductoPort {

    /**
     * Busca un producto por su id y retorna un snapshot de sus datos.
     *
     * @param id identificador del producto.
     * @return un {@link Optional} con el snapshot si existe, vacío si no.
     */
    Optional<ProductoSnapshot> findById(Long id);
}
