package com.barquito.productos.application;

import java.util.List;

/**
 * Puerto de entrada (caso de uso) para la gestión del catálogo de productos.
 *
 * <p>Define las operaciones disponibles sobre productos. La implementación
 * {@link ProductoServiceImpl} aplica las reglas de negocio y delega la
 * persistencia al {@code ProductoRepository}.
 */
public interface ProductoService {

    /**
     * Crea un nuevo producto en el catálogo.
     *
     * @param command datos del nuevo producto.
     * @return el producto creado como {@link ProductoResponse}.
     * @throws com.barquito.productos.domain.ProductoNombreDuplicadoException si ya existe un producto
     *         con el mismo nombre (case-insensitive).
     */
    ProductoResponse crearProducto(CrearProductoCommand command);

    /**
     * Obtiene un producto por su identificador.
     *
     * @param id identificador del producto.
     * @return el producto como {@link ProductoResponse}.
     * @throws com.barquito.productos.domain.ProductoNotFoundException si no existe un producto con ese id.
     */
    ProductoResponse obtenerProducto(Long id);

    /**
     * Lista todos los productos activos del catálogo.
     *
     * @return lista de {@link ProductoResponse} de todos los productos con {@code activo = true}.
     */
    List<ProductoResponse> listarProductos();

    /**
     * Lista los productos activos y disponibles para ser pedidos.
     *
     * @return lista de {@link ProductoResponse} con {@code activo = true} y {@code disponible = true}.
     */
    List<ProductoResponse> listarProductosDisponibles();

    /**
     * Actualiza los datos de un producto existente.
     *
     * @param id      identificador del producto a actualizar.
     * @param command nuevos datos del producto.
     * @return el producto actualizado como {@link ProductoResponse}.
     * @throws com.barquito.productos.domain.ProductoNotFoundException si no existe un producto con ese id.
     */
    ProductoResponse actualizarProducto(Long id, ActualizarProductoCommand command);

    /**
     * Desactiva (soft delete) un producto del catálogo.
     *
     * <p>El producto no se elimina físicamente; se marca con {@code activo = false}.
     *
     * @param id identificador del producto a desactivar.
     * @throws com.barquito.productos.domain.ProductoNotFoundException si no existe un producto con ese id.
     */
    void desactivarProducto(Long id);
}
