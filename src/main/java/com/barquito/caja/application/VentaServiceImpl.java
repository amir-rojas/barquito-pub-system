package com.barquito.caja.application;

import com.barquito.caja.domain.DetalleVenta;
import com.barquito.caja.domain.DetalleVentaRepository;
import com.barquito.caja.domain.EstadoVenta;
import com.barquito.caja.domain.MetodoPago;
import com.barquito.caja.domain.Venta;
import com.barquito.caja.domain.VentaNotFoundException;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import com.barquito.caja.domain.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del puerto de entrada {@link VentaService}.
 *
 * <p>Coordina los puertos de salida para orquestar el ciclo de vida de una venta:
 * creación desde pedido, cobro atómico con liberación de mesa, y anulación.
 */
@Service
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final PedidoPort pedidoPort;
    private final MesaLiberarPort mesaLiberarPort;
    private final UsuarioLookupPort usuarioLookupPort;
    private final RegistrarTransaccionPort registrarTransaccionPort;

    /**
     * Inyección por constructor.
     */
    public VentaServiceImpl(
            final VentaRepository ventaRepository,
            final DetalleVentaRepository detalleVentaRepository,
            final PedidoPort pedidoPort,
            final MesaLiberarPort mesaLiberarPort,
            final UsuarioLookupPort usuarioLookupPort,
            final RegistrarTransaccionPort registrarTransaccionPort) {
        this.ventaRepository = ventaRepository;
        this.detalleVentaRepository = detalleVentaRepository;
        this.pedidoPort = pedidoPort;
        this.mesaLiberarPort = mesaLiberarPort;
        this.usuarioLookupPort = usuarioLookupPort;
        this.registrarTransaccionPort = registrarTransaccionPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VentaConDetalles crearVenta(final Long pedidoId, final String cajeroUsername) {
        // 1. Resolve cajero
        final Long cajeroId = usuarioLookupPort.resolverIdPorUsername(cajeroUsername);

        // 2. Load + validate pedido is CERRADO
        final PedidoSnapshot pedido = pedidoPort.findSnapshot(pedidoId)
                .orElseThrow(() -> new VentaOperacionInvalidaException(
                        "Pedido no encontrado: " + pedidoId));
        if (pedido.estado() != EstadoPedidoSnapshot.CERRADO) {
            throw new VentaOperacionInvalidaException(
                    "Solo se puede crear venta de un pedido CERRADO. Estado actual: " + pedido.estado());
        }

        // 3. Defensive idempotency check (race-safe via DB UNIQUE on pedido_id)
        if (ventaRepository.existsByPedidoId(pedidoId)) {
            throw new VentaOperacionInvalidaException(
                    "Ya existe una venta para el pedido " + pedidoId);
        }

        // 4. Load lineas, filter CANCELADO
        final List<LineaPedidoSnapshot> facturables = pedidoPort
                .findLineasByPedidoId(pedidoId).stream()
                .filter(l -> l.estado() != EstadoLineaSnapshot.CANCELADO)
                .toList();
        if (facturables.isEmpty()) {
            throw new VentaOperacionInvalidaException(
                    "No hay líneas facturables en el pedido " + pedidoId);
        }

        // 5. Compute total = SUM(linea.subtotal)
        final BigDecimal total = facturables.stream()
                .map(LineaPedidoSnapshot::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. INSERT venta in PENDIENTE
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Venta nueva = new Venta(null, pedidoId, pedido.mesaId(), cajeroId,
                total, null, EstadoVenta.PENDIENTE, ahora, null, null);
        final Venta guardada = ventaRepository.save(nueva);

        // 7. INSERT detalle_ventas (snapshot)
        final List<DetalleVenta> detallesSnapshot = facturables.stream()
                .map(l -> new DetalleVenta(null, guardada.id(), l.productoId(),
                        l.productoNombre(), l.cantidad(), l.precioUnitario(), null))
                .toList();
        final List<DetalleVenta> detallesGuardados = detalleVentaRepository.saveAll(detallesSnapshot);

        return new VentaConDetalles(guardada, detallesGuardados);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VentaConDetalles cobrarVenta(final Long ventaId, final MetodoPago metodoPago) {
        // 1. Load venta
        final Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));

        // 2. Domain transition (throws VentaOperacionInvalidaException if not PENDIENTE)
        final Venta cobrada = venta.cobrar(metodoPago, OffsetDateTime.now());

        // 3. UPDATE venta
        final Venta guardada = ventaRepository.save(cobrada);

        // 4. Liberar mesa (PESSIMISTIC_WRITE, MANDATORY — same TX)
        mesaLiberarPort.liberarMesa(guardada.mesaId());

        // 5. Registrar ingreso en finanzas
        registrarTransaccionPort.registrarIngreso(
                guardada.id(),
                guardada.total(),
                "Cobro venta #" + guardada.id() + " - " + metodoPago.name(),
                guardada.cajeroId()
        );

        // 6. Load detalles for response
        final List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        return new VentaConDetalles(guardada, detalles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VentaConDetalles anularVenta(final Long ventaId) {
        final Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));
        final Venta anulada = venta.anular(OffsetDateTime.now());
        final Venta guardada = ventaRepository.save(anulada);
        final List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        return new VentaConDetalles(guardada, detalles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public VentaConDetalles buscarVenta(final Long ventaId) {
        final Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new VentaNotFoundException(ventaId));
        final List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        return new VentaConDetalles(venta, detalles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<VentaConDetalles> buscarPorPedido(final Long pedidoId) {
        return ventaRepository.findByPedidoId(pedidoId)
                .map(v -> new VentaConDetalles(v, detalleVentaRepository.findByVentaId(v.id())));
    }
}
