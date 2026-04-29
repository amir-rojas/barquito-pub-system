package com.barquito.pedidos.infrastructure;

import com.barquito.pedidos.application.ProductoPort;
import com.barquito.pedidos.application.ProductoSnapshot;
import com.barquito.productos.application.ProductoService;
import com.barquito.productos.domain.ProductoNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador de salida que implementa {@link ProductoPort} delegando al módulo de productos.
 *
 * <p>Obtiene los datos del producto a través de {@link ProductoService}, respetando
 * la arquitectura hexagonal entre bounded contexts.
 */
@Component
public class ProductoJpaAdapter implements ProductoPort {

    private final ProductoService productoService;

    /**
     * Construye el adaptador con el servicio de productos.
     *
     * @param productoService servicio del bounded context de productos.
     */
    public ProductoJpaAdapter(final ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delega al {@link ProductoService} del módulo de productos y convierte
     * el {@code ProductoResponse} en un {@link ProductoSnapshot}.
     */
    @Override
    public Optional<ProductoSnapshot> findById(final Long id) {
        try {
            final var response = productoService.obtenerProducto(id);
            return Optional.of(new ProductoSnapshot(
                    response.id(),
                    response.nombre(),
                    response.precio(),
                    response.activo()
            ));
        } catch (final ProductoNotFoundException e) {
            return Optional.empty();
        }
    }
}
