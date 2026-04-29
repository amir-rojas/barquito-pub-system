package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.LineaPedidoRepository;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.pedidos.domain.PedidoNotFoundException;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del caso de uso de gestión de pedidos.
 *
 * <p>Aplica todas las invariantes de negocio:
 * <ul>
 *   <li>{@link #crearPedido}: adquiere lock PESSIMISTIC_WRITE en mesa via
 *       {@link MesaStatusPort#ocupar(Long)}.</li>
 *   <li>{@link #cerrarPedido}: solo pedido ABIERTO; si es el último → CUENTA_PEDIDA.</li>
 *   <li>{@link #cancelarPedido}: cascada CANCELADO en líneas activas;
 *       regla de mesa por ENTREGADO.</li>
 * </ul>
 */
@Service
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;
    private final MesaStatusPort mesaStatusPort;
    private final UsuarioLookupPort usuarioLookupPort;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param pedidoRepository      puerto de salida para pedidos.
     * @param lineaPedidoRepository puerto de salida para líneas de pedido.
     * @param mesaStatusPort        puerto de salida para transiciones de mesa.
     * @param usuarioLookupPort     puerto de salida para resolución de usuario.
     */
    public PedidoServiceImpl(final PedidoRepository pedidoRepository,
                              final LineaPedidoRepository lineaPedidoRepository,
                              final MesaStatusPort mesaStatusPort,
                              final UsuarioLookupPort usuarioLookupPort) {
        this.pedidoRepository = pedidoRepository;
        this.lineaPedidoRepository = lineaPedidoRepository;
        this.mesaStatusPort = mesaStatusPort;
        this.usuarioLookupPort = usuarioLookupPort;
    }

    @Override
    public Pedido crearPedido(final Long mesaId, final String nombreMesero, final String notas) {
        final Long meseroId = usuarioLookupPort.findIdByNombre(nombreMesero);
        // Acquires PESSIMISTIC_WRITE on mesa row + transitions to OCUPADA
        // Throws PedidoOperacionInvalidaException if mesa is FUSIONADA or inactive
        mesaStatusPort.ocupar(mesaId);

        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido nuevo = new Pedido(null, mesaId, meseroId,
                EstadoPedido.ABIERTO, notas, ahora, ahora, null);
        return pedidoRepository.save(nuevo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosByMesa(final Long mesaId) {
        return pedidoRepository.findByMesaId(mesaId);
    }

    @Override
    @Transactional(readOnly = true)
    public PedidoConLineas buscarPedidoConLineas(final Long id) {
        final Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        final List<LineaPedido> lineas = lineaPedidoRepository.findByPedidoId(id);
        return new PedidoConLineas(pedido, lineas);
    }

    @Override
    public Pedido actualizarNotas(final Long id, final String notas) {
        final Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        if (pedido.estado() != EstadoPedido.ABIERTO) {
            throw new PedidoOperacionInvalidaException(
                    "Solo se pueden actualizar notas de un pedido ABIERTO. Estado actual: "
                            + pedido.estado());
        }
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido actualizado = new Pedido(pedido.id(), pedido.mesaId(), pedido.meseroId(),
                pedido.estado(), notas, pedido.creadoEn(), ahora, pedido.cerradoEn());
        return pedidoRepository.save(actualizado);
    }

    @Override
    public Pedido cerrarPedido(final Long id) {
        final Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        if (pedido.estado() != EstadoPedido.ABIERTO) {
            throw new PedidoOperacionInvalidaException(
                    "Solo se puede cerrar un pedido ABIERTO. Estado actual: " + pedido.estado());
        }

        // FR-P11 / INV-02: no se puede cerrar si hay líneas sin entregar
        final List<LineaPedido> lineas = lineaPedidoRepository.findByPedidoId(id);
        final boolean hayLineasSinEntregar = lineas.stream()
                .anyMatch(l -> l.estado() != EstadoLinea.ENTREGADO
                        && l.estado() != EstadoLinea.CANCELADO);
        if (hayLineasSinEntregar) {
            throw new PedidoOperacionInvalidaException(
                    "No se puede cerrar el pedido: hay líneas sin entregar");
        }

        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido cerrado = new Pedido(pedido.id(), pedido.mesaId(), pedido.meseroId(),
                EstadoPedido.CERRADO, pedido.notas(), pedido.creadoEn(), ahora, ahora);
        final Pedido guardado = pedidoRepository.save(cerrado);

        // Si no quedan pedidos abiertos en la mesa → CUENTA_PEDIDA
        final long restantes = pedidoRepository.countAbiertosByMesaId(pedido.mesaId());
        if (restantes == 0) {
            mesaStatusPort.transicionarACuentaPedida(pedido.mesaId());
        }
        return guardado;
    }

    @Override
    public Pedido cancelarPedido(final Long id) {
        final Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new PedidoNotFoundException(id));
        if (pedido.estado() != EstadoPedido.ABIERTO) {
            throw new PedidoOperacionInvalidaException(
                    "Solo se puede cancelar un pedido ABIERTO. Estado actual: " + pedido.estado());
        }

        // Cancelar líneas activas
        final List<LineaPedido> lineas = lineaPedidoRepository.findByPedidoId(id);
        final OffsetDateTime ahora = OffsetDateTime.now();
        final List<LineaPedido> lineasActualizadas = lineas.stream()
                .filter(l -> l.estado() != EstadoLinea.CANCELADO
                        && l.estado() != EstadoLinea.ENTREGADO)
                .map(l -> new LineaPedido(l.id(), l.pedidoId(), l.productoId(),
                        l.cantidad(), l.precioUnitario(), l.subtotal(),
                        EstadoLinea.CANCELADO, l.notas(), l.creadoEn(), ahora))
                .toList();
        if (!lineasActualizadas.isEmpty()) {
            lineaPedidoRepository.saveAll(lineasActualizadas);
        }

        final Pedido cancelado = new Pedido(pedido.id(), pedido.mesaId(), pedido.meseroId(),
                EstadoPedido.CANCELADO, pedido.notas(), pedido.creadoEn(), ahora, ahora);
        final Pedido guardado = pedidoRepository.save(cancelado);

        // Regla de mesa: si no quedan pedidos abiertos
        final long restantes = pedidoRepository.countAbiertosByMesaId(pedido.mesaId());
        if (restantes == 0) {
            final boolean hayEntregados = pedidoRepository.existsEntregadaLineaByMesaId(pedido.mesaId());
            if (hayEntregados) {
                mesaStatusPort.transicionarACuentaPedida(pedido.mesaId());
            } else {
                mesaStatusPort.liberarMesa(pedido.mesaId());
            }
        }
        return guardado;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PedidoConLineas> buscarPedidoActivoPorMesa(final Long mesaId) {
        return pedidoRepository.findAbiertoByMesaId(mesaId)
                .map(p -> new PedidoConLineas(p, lineaPedidoRepository.findByPedidoId(p.id())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPedidosAbiertosByMesa(final Long mesaId) {
        return pedidoRepository.findByMesaIdAndEstado(mesaId, EstadoPedido.ABIERTO);
    }
}
