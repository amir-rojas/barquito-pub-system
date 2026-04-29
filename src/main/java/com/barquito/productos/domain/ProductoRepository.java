package com.barquito.productos.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (repositorio) para el dominio de productos.
 *
 * <p>Define el contrato que la capa de infraestructura debe implementar.
 * No tiene dependencias de JPA ni de ningún framework.
 */
public interface ProductoRepository {

    /**
     * Persiste un producto (insert o update).
     *
     * @param producto producto a guardar.
     * @return el producto persistido con su {@code id} asignado.
     */
    Producto save(Producto producto);

    /**
     * Busca un producto por su identificador.
     *
     * @param id identificador del producto.
     * @return un {@link Optional} con el producto si existe, vacío si no.
     */
    Optional<Producto> findById(Long id);

    /**
     * Busca un producto por nombre, ignorando mayúsculas/minúsculas.
     *
     * @param nombre nombre del producto a buscar.
     * @return un {@link Optional} con el producto si existe, vacío si no.
     */
    Optional<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Retorna todos los productos activos ({@code activo = true}).
     *
     * @return lista de productos activos; vacía si no hay ninguno.
     */
    List<Producto> findAllActivos();

    /**
     * Retorna todos los productos activos y disponibles ({@code activo = true} y {@code disponible = true}).
     *
     * @return lista de productos activos y disponibles; vacía si no hay ninguno.
     */
    List<Producto> findAllActivosYDisponibles();
}
