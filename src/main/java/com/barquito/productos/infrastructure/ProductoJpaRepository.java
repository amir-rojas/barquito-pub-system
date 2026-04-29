package com.barquito.productos.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad {@link ProductoEntity}.
 *
 * <p>Spring Data JPA genera la implementación en tiempo de ejecución.
 * Solo se usa dentro del adaptador {@link ProductoJpaAdapter}.
 */
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Long> {

    /**
     * Busca un producto por nombre ignorando mayúsculas/minúsculas.
     *
     * @param nombre nombre del producto a buscar.
     * @return el producto si existe, vacío si no.
     */
    Optional<ProductoEntity> findByNombreIgnoreCase(String nombre);

    /**
     * Retorna todos los productos con {@code activo = true}.
     *
     * @return lista de productos activos.
     */
    List<ProductoEntity> findAllByActivoTrue();

    /**
     * Retorna todos los productos con {@code activo = true} y {@code disponible = true}.
     *
     * @return lista de productos activos y disponibles.
     */
    List<ProductoEntity> findAllByActivoTrueAndDisponibleTrue();
}
