package com.barquito.caja.api;

import com.barquito.caja.application.VentaConDetalles;
import com.barquito.caja.application.VentaService;
import com.barquito.caja.domain.DetalleVenta;
import com.barquito.caja.domain.Venta;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para el contexto de caja.
 *
 * <p>Base path: {@code /api/caja/ventas}.
 * Todas las operaciones requieren autenticación JWT.
 */
@RestController
@RequestMapping("/api/caja")
public class VentaController {

    private final VentaService ventaService;

    /**
     * Inyección por constructor.
     */
    public VentaController(final VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Crea una venta en estado PENDIENTE desde un pedido CERRADO.
     *
     * @param request   body con {@code pedidoId}.
     * @param principal usuario autenticado (extraído del JWT).
     * @return 201 Created con la venta y sus detalles.
     */
    @PostMapping("/ventas")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<VentaResponse> crear(
            @Valid @RequestBody final CrearVentaRequest request,
            @AuthenticationPrincipal final UserDetails principal) {
        final VentaConDetalles cd = ventaService.crearVenta(
                request.pedidoId(), principal.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cd));
    }

    /**
     * Cobra una venta PENDIENTE con el método de pago indicado.
     *
     * @param id      identificador de la venta.
     * @param request body con {@code metodoPago}.
     * @return 200 OK con la venta actualizada.
     */
    @PostMapping("/ventas/{id}/cobrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<VentaResponse> cobrar(
            @PathVariable final Long id,
            @Valid @RequestBody final CobrarVentaRequest request) {
        return ResponseEntity.ok(toResponse(
                ventaService.cobrarVenta(id, request.metodoPago())));
    }

    /**
     * Anula una venta PENDIENTE. Solo ADMIN puede ejecutar esta operación.
     *
     * @param id identificador de la venta.
     * @return 200 OK con la venta actualizada.
     */
    @PostMapping("/ventas/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentaResponse> anular(@PathVariable final Long id) {
        return ResponseEntity.ok(toResponse(ventaService.anularVenta(id)));
    }

    /**
     * Retorna una venta por su identificador junto con sus detalles.
     *
     * @param id identificador de la venta.
     * @return 200 OK con la venta y sus detalles.
     */
    @GetMapping("/ventas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<VentaResponse> buscarPorId(@PathVariable final Long id) {
        return ResponseEntity.ok(toResponse(ventaService.buscarVenta(id)));
    }

    /**
     * Retorna la venta asociada a un pedido.
     *
     * @param pedidoId identificador del pedido.
     * @return 200 OK si existe, 404 Not Found si no hay venta para ese pedido.
     */
    @GetMapping("/ventas")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<VentaResponse> buscarPorPedido(
            @RequestParam final Long pedidoId) {
        final Optional<VentaConDetalles> opt = ventaService.buscarPorPedido(pedidoId);
        return opt.map(cd -> ResponseEntity.ok(toResponse(cd)))
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private VentaResponse toResponse(final VentaConDetalles cd) {
        final Venta v = cd.venta();
        final List<DetalleVentaResponse> detalles = cd.detalles().stream()
                .map(this::toDetalleResponse)
                .toList();
        return new VentaResponse(
                v.id(), v.pedidoId(), v.mesaId(), v.cajeroId(), v.total(),
                v.metodoPago() == null ? null : v.metodoPago().name(),
                v.estado().name(),
                v.creadoEn(), v.pagadoEn(), v.anuladoEn(),
                detalles
        );
    }

    private DetalleVentaResponse toDetalleResponse(final DetalleVenta d) {
        return new DetalleVentaResponse(
                d.id(), d.productoId(), d.productoNombre(),
                d.cantidad(), d.precioUnitario(), d.subtotal());
    }
}
