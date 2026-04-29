package com.barquito.pedidos.api;

import com.barquito.pedidos.application.LineaPedidoService;
import com.barquito.pedidos.domain.LineaPedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para operaciones sobre líneas de pedido.
 *
 * <p>Todos los endpoints requieren rol ADMIN o MESERO.
 * La validación per-transición de roles se delega al servicio.
 */
@RestController
@RequestMapping("/api/pedidos/{pedidoId}/lineas")
public class LineaPedidoController {

    private final LineaPedidoService lineaPedidoService;

    /**
     * Construye el controlador con el servicio de líneas.
     *
     * @param lineaPedidoService puerto de entrada para operaciones sobre líneas.
     */
    public LineaPedidoController(final LineaPedidoService lineaPedidoService) {
        this.lineaPedidoService = lineaPedidoService;
    }

    /**
     * Agrega una línea al pedido.
     *
     * @param pedidoId id del pedido.
     * @param request  datos de la línea.
     * @return 201 Created con la línea creada.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<LineaPedidoResponse> agregar(
            @PathVariable final Long pedidoId,
            @Valid @RequestBody final CrearLineaPedidoRequest request) {
        final LineaPedido linea = lineaPedidoService.agregarLinea(
                pedidoId, request.productoId(), request.cantidad(), request.notas());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(linea));
    }

    /**
     * Lista las líneas de un pedido.
     *
     * @param pedidoId id del pedido.
     * @return 200 OK con lista de líneas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<List<LineaPedidoResponse>> listar(@PathVariable final Long pedidoId) {
        final List<LineaPedidoResponse> lineas = lineaPedidoService.listarLineas(pedidoId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(lineas);
    }

    /**
     * Busca una línea por id.
     *
     * @param pedidoId  id del pedido.
     * @param lineaId   id de la línea.
     * @return 200 OK con la línea encontrada.
     */
    @GetMapping("/{lineaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<LineaPedidoResponse> buscarPorId(
            @PathVariable final Long pedidoId,
            @PathVariable final Long lineaId) {
        return ResponseEntity.ok(toResponse(lineaPedidoService.buscarLinea(pedidoId, lineaId)));
    }

    /**
     * Actualiza cantidad y/o notas de una línea PENDIENTE.
     *
     * @param pedidoId id del pedido.
     * @param lineaId  id de la línea.
     * @param request  nuevos valores.
     * @return 200 OK con la línea actualizada.
     */
    @PatchMapping("/{lineaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<LineaPedidoResponse> actualizar(
            @PathVariable final Long pedidoId,
            @PathVariable final Long lineaId,
            @Valid @RequestBody final ActualizarLineaPedidoRequest request) {
        final LineaPedido linea = lineaPedidoService.actualizarLinea(
                pedidoId, lineaId, request.cantidad(), request.notas());
        return ResponseEntity.ok(toResponse(linea));
    }

    /**
     * Cambia el estado de una línea (con validación per-transición de rol en el servicio).
     *
     * @param pedidoId       id del pedido.
     * @param lineaId        id de la línea.
     * @param request        estado destino.
     * @param authentication usuario autenticado (para extraer el rol).
     * @return 200 OK con la línea actualizada.
     */
    @PatchMapping("/{lineaId}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<LineaPedidoResponse> cambiarEstado(
            @PathVariable final Long pedidoId,
            @PathVariable final Long lineaId,
            @Valid @RequestBody final CambiarEstadoLineaRequest request,
            final Authentication authentication) {
        final String rol = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
        final LineaPedido linea = lineaPedidoService.cambiarEstadoLinea(lineaId, request.estado(), rol);
        return ResponseEntity.ok(toResponse(linea));
    }

    /**
     * Cancela una línea PENDIENTE (alias DELETE 204).
     *
     * @param pedidoId id del pedido.
     * @param lineaId  id de la línea.
     * @return 204 No Content.
     */
    @DeleteMapping("/{lineaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<Void> cancelar(
            @PathVariable final Long pedidoId,
            @PathVariable final Long lineaId,
            final Authentication authentication) {
        final String rol = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
        lineaPedidoService.cancelarLinea(pedidoId, lineaId, rol);
        return ResponseEntity.noContent().build();
    }

    private LineaPedidoResponse toResponse(final LineaPedido linea) {
        return new LineaPedidoResponse(
                linea.id(),
                linea.pedidoId(),
                linea.productoId(),
                linea.cantidad(),
                linea.precioUnitario(),
                linea.subtotal(),
                linea.estado().name(),
                linea.notas(),
                linea.creadoEn(),
                linea.actualizadoEn()
        );
    }
}
