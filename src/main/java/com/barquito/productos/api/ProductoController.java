package com.barquito.productos.api;

import com.barquito.productos.application.ActualizarProductoCommand;
import com.barquito.productos.application.CrearProductoCommand;
import com.barquito.productos.application.ProductoResponse;
import com.barquito.productos.application.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión del catálogo de productos.
 *
 * <p>Expone los endpoints del bounded context {@code productos}.
 * La seguridad se aplica a nivel de método con {@code @PreAuthorize}.
 * No tiene anotación {@code @Transactional}: la transaccionalidad se maneja en el servicio.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    /**
     * Construye el controlador con el servicio de productos.
     *
     * @param productoService servicio de gestión de productos.
     */
    public ProductoController(final ProductoService productoService) {
        this.productoService = productoService;
    }

    /**
     * Crea un nuevo producto en el catálogo.
     *
     * <p>Solo accesible por usuarios con rol ADMIN.
     *
     * @param request datos del nuevo producto.
     * @return 201 Created con el producto creado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> crearProducto(
            @Valid @RequestBody final CrearProductoRequest request) {
        final CrearProductoCommand command = new CrearProductoCommand(
                request.nombre(),
                request.precio(),
                request.descripcion(),
                request.categoria(),
                request.disponible()
        );
        final ProductoResponse response = productoService.crearProducto(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista todos los productos activos del catálogo.
     *
     * <p>Accesible por ADMIN y MESERO.
     *
     * @return 200 OK con la lista de productos activos.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    public ResponseEntity<List<ProductoResponse>> listarProductos() {
        return ResponseEntity.ok(productoService.listarProductos());
    }

    /**
     * Lista los productos activos y disponibles para ser pedidos.
     *
     * <p>Accesible por ADMIN y MESERO.
     *
     * @return 200 OK con la lista de productos disponibles.
     */
    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    public ResponseEntity<List<ProductoResponse>> listarProductosDisponibles() {
        return ResponseEntity.ok(productoService.listarProductosDisponibles());
    }

    /**
     * Obtiene un producto por su identificador.
     *
     * <p>Accesible por ADMIN y MESERO.
     *
     * @param id identificador del producto.
     * @return 200 OK con el producto encontrado.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MESERO')")
    public ResponseEntity<ProductoResponse> obtenerProducto(@PathVariable final Long id) {
        return ResponseEntity.ok(productoService.obtenerProducto(id));
    }

    /**
     * Actualiza los datos de un producto existente.
     *
     * <p>Solo accesible por usuarios con rol ADMIN.
     *
     * @param id      identificador del producto a actualizar.
     * @param request nuevos datos del producto.
     * @return 200 OK con el producto actualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductoResponse> actualizarProducto(
            @PathVariable final Long id,
            @Valid @RequestBody final ActualizarProductoRequest request) {
        final ActualizarProductoCommand command = new ActualizarProductoCommand(
                request.nombre(),
                request.precio(),
                request.descripcion(),
                request.categoria(),
                request.disponible()
        );
        return ResponseEntity.ok(productoService.actualizarProducto(id, command));
    }

    /**
     * Desactiva (soft delete) un producto del catálogo.
     *
     * <p>Solo accesible por usuarios con rol ADMIN. El producto no se elimina
     * físicamente; se marca con {@code activo = false}.
     *
     * @param id identificador del producto a desactivar.
     * @return 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivarProducto(@PathVariable final Long id) {
        productoService.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
