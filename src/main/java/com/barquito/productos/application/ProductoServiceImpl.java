package com.barquito.productos.application;

import com.barquito.productos.domain.Producto;
import com.barquito.productos.domain.ProductoNombreDuplicadoException;
import com.barquito.productos.domain.ProductoNotFoundException;
import com.barquito.productos.domain.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementación de {@link ProductoService} que aplica las reglas de negocio
 * del catálogo de productos.
 *
 * <p>Coordina las operaciones de dominio con el repositorio de productos.
 * Las operaciones de escritura están anotadas con {@code @Transactional}.
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    /**
     * Construye el servicio con su repositorio de dominio.
     *
     * @param productoRepository repositorio de productos.
     */
    public ProductoServiceImpl(final ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Verifica que no exista otro producto con el mismo nombre (case-insensitive)
     * antes de persistir.
     */
    @Override
    @Transactional
    public ProductoResponse crearProducto(final CrearProductoCommand command) {
        productoRepository.findByNombreIgnoreCase(command.nombre())
                .ifPresent(p -> {
                    throw new ProductoNombreDuplicadoException(
                            "Ya existe un producto con el nombre: " + command.nombre());
                });

        final Producto nuevo = new Producto(
                null,
                command.nombre(),
                command.precio(),
                command.descripcion(),
                command.categoria(),
                command.disponible(),
                true,
                null
        );

        final Producto guardado = productoRepository.save(nuevo);
        return ProductoResponse.from(guardado);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerProducto(final Long id) {
        return productoRepository.findById(id)
                .map(ProductoResponse::from)
                .orElseThrow(() -> new ProductoNotFoundException(
                        "Producto no encontrado con id: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos() {
        return productoRepository.findAllActivos().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductosDisponibles() {
        return productoRepository.findAllActivosYDisponibles().stream()
                .map(ProductoResponse::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public ProductoResponse actualizarProducto(final Long id, final ActualizarProductoCommand command) {
        final Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(
                        "Producto no encontrado con id: " + id));

        productoRepository.findByNombreIgnoreCase(command.nombre())
                .filter(p -> !p.id().equals(id))
                .ifPresent(p -> {
                    throw new ProductoNombreDuplicadoException(
                            "Ya existe un producto con el nombre: " + command.nombre());
                });

        final Producto actualizado = existente.actualizar(
                command.nombre(),
                command.precio(),
                command.descripcion(),
                command.categoria(),
                command.disponible()
        );

        return ProductoResponse.from(productoRepository.save(actualizado));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void desactivarProducto(final Long id) {
        final Producto existente = productoRepository.findById(id)
                .orElseThrow(() -> new ProductoNotFoundException(
                        "Producto no encontrado con id: " + id));

        productoRepository.save(existente.desactivar());
    }
}
