package com.barquito.productos.infrastructure;

import com.barquito.productos.domain.Producto;
import com.barquito.productos.domain.ProductoRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link ProductoRepository} usando JPA.
 *
 * <p>Traduce entre objetos de dominio ({@link Producto}) y entidades JPA ({@link ProductoEntity}).
 * El nombre explícito del componente evita colisiones con el bean de mismo nombre en pedidos.
 */
@Component("productosProductoJpaAdapter")
public class ProductoJpaAdapter implements ProductoRepository {

    private final ProductoJpaRepository jpaRepository;

    /**
     * Construye el adaptador con el repositorio JPA.
     *
     * @param jpaRepository repositorio JPA de productos.
     */
    public ProductoJpaAdapter(final ProductoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Producto save(final Producto producto) {
        final ProductoEntity entity = ProductoEntity.toEntity(producto);
        return jpaRepository.save(entity).toDomain();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Producto> findById(final Long id) {
        return jpaRepository.findById(id).map(ProductoEntity::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Producto> findByNombreIgnoreCase(final String nombre) {
        return jpaRepository.findByNombreIgnoreCase(nombre).map(ProductoEntity::toDomain);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Producto> findAllActivos() {
        return jpaRepository.findAllByActivoTrue().stream()
                .map(ProductoEntity::toDomain)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Producto> findAllActivosYDisponibles() {
        return jpaRepository.findAllByActivoTrueAndDisponibleTrue().stream()
                .map(ProductoEntity::toDomain)
                .toList();
    }
}
