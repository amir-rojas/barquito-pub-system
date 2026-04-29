package com.barquito.pedidos.api;

import com.barquito.pedidos.application.PedidoConLineas;
import com.barquito.pedidos.application.PedidoService;
import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.Pedido;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para operaciones sobre pedidos.
 *
 * <p>Todos los endpoints requieren rol ADMIN o MESERO.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    /**
     * Construye el controlador con el servicio de pedidos.
     *
     * @param pedidoService puerto de entrada para operaciones sobre pedidos.
     */
    public PedidoController(final PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    /**
     * Crea un nuevo pedido para una mesa.
     *
     * @param request   datos del pedido.
     * @param principal usuario autenticado (mesero).
     * @return 201 Created con el pedido creado.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<PedidoResponse> crear(
            @Valid @RequestBody final CrearPedidoRequest request,
            @AuthenticationPrincipal final UserDetails principal) {
        final Pedido pedido = pedidoService.crearPedido(
                request.mesaId(), principal.getUsername(), request.notas());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(pedido, null));
    }

    /**
     * Lista pedidos ABIERTOS de una mesa (uso POS).
     *
     * @param mesaId id de la mesa.
     * @return 200 OK con lista de pedidos en estado ABIERTO.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<List<PedidoResponse>> listar(
            @RequestParam final Long mesaId) {
        final List<PedidoResponse> pedidos = pedidoService.listarPedidosAbiertosByMesa(mesaId)
                .stream()
                .map(p -> toResponse(p, null))
                .toList();
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Retorna el pedido ABIERTO de una mesa, o 404 si no hay pedido activo.
     *
     * <p>Endpoint de uso frecuente por el frontend POS: al tocar una mesa,
     * el frontend consulta este endpoint para saber si hay un pedido activo.
     *
     * @param mesaId id de la mesa.
     * @return 200 OK con el pedido activo e incluye sus líneas; 404 si no hay pedido abierto.
     */
    @GetMapping("/activo")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<PedidoResponse> buscarActivo(@RequestParam final Long mesaId) {
        final Optional<PedidoConLineas> resultado = pedidoService.buscarPedidoActivoPorMesa(mesaId);
        if (resultado.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        final PedidoConLineas conLineas = resultado.get();
        final List<LineaPedidoResponse> lineas = conLineas.lineas().stream()
                .map(this::toLineaResponse)
                .toList();
        return ResponseEntity.ok(toResponse(conLineas.pedido(), lineas));
    }

    /**
     * Busca un pedido por id, incluyendo sus líneas.
     *
     * @param id id del pedido.
     * @return 200 OK con el pedido y sus líneas.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<PedidoResponse> buscarPorId(@PathVariable final Long id) {
        final PedidoConLineas conLineas = pedidoService.buscarPedidoConLineas(id);
        final List<LineaPedidoResponse> lineas = conLineas.lineas().stream()
                .map(this::toLineaResponse)
                .toList();
        return ResponseEntity.ok(toResponse(conLineas.pedido(), lineas));
    }

    /**
     * Actualiza las notas de un pedido ABIERTO.
     *
     * @param id      id del pedido.
     * @param request nuevas notas.
     * @return 200 OK con el pedido actualizado.
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<PedidoResponse> actualizarNotas(
            @PathVariable final Long id,
            @Valid @RequestBody final ActualizarPedidoRequest request) {
        final Pedido pedido = pedidoService.actualizarNotas(id, request.notas());
        return ResponseEntity.ok(toResponse(pedido, null));
    }

    /**
     * Cierra un pedido.
     *
     * @param id id del pedido.
     * @return 200 OK con el pedido cerrado.
     */
    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<PedidoResponse> cerrar(@PathVariable final Long id) {
        final Pedido pedido = pedidoService.cerrarPedido(id);
        return ResponseEntity.ok(toResponse(pedido, null));
    }

    /**
     * Cancela un pedido. Solo ADMIN.
     *
     * @param id id del pedido.
     * @return 200 OK con el pedido cancelado.
     */
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable final Long id) {
        final Pedido pedido = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(toResponse(pedido, null));
    }

    private PedidoResponse toResponse(final Pedido pedido, final List<LineaPedidoResponse> lineas) {
        return new PedidoResponse(
                pedido.id(),
                pedido.mesaId(),
                pedido.meseroId(),
                pedido.estado().name(),
                pedido.notas(),
                pedido.creadoEn(),
                pedido.actualizadoEn(),
                pedido.cerradoEn(),
                lineas
        );
    }

    private LineaPedidoResponse toLineaResponse(final LineaPedido linea) {
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
