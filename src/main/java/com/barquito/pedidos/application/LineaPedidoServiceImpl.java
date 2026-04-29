package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.LineaPedidoNotFoundException;
import com.barquito.pedidos.domain.LineaPedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.LineaPedidoRepository;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.pedidos.domain.PedidoNotFoundException;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.PedidoRepository;
import com.barquito.pedidos.domain.ProductoNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementación del caso de uso de gestión de líneas de pedido.
 *
 * <p>Aplica la matriz de roles por transición de estado:
 * <ul>
 *   <li>PENDIENTE → EN_PREPARACION : ADMIN, MESERO</li>
 *   <li>EN_PREPARACION → LISTO     : ADMIN, MESERO</li>
 *   <li>LISTO → ENTREGADO          : ADMIN, MESERO</li>
 *   <li>PENDIENTE → CANCELADO      : ADMIN, MESERO</li>
 *   <li>EN_PREPARACION → CANCELADO : ADMIN</li>
 *   <li>LISTO → CANCELADO          : ADMIN</li>
 * </ul>
 *
 * <p>NOTA sobre roles: El design doc original mencionaba BARTENDER para ciertas
 * transiciones. Por decisión final del proyecto, BARTENDER no se usa en pedidos;
 * solo ADMIN y MESERO operan este módulo.
 */
@Service
@Transactional
public class LineaPedidoServiceImpl implements LineaPedidoService {

    /**
     * Mapa de roles permitidos por clave de transición (origen → destino).
     *
     * <p>La ausencia de una clave en el mapa significa que la transición es inválida
     * independientemente del rol.
     */
    private static final Map<TransitionKey, Set<String>> ROLES_PERMITIDOS = Map.of(
            new TransitionKey(EstadoLinea.PENDIENTE, EstadoLinea.EN_PREPARACION),
                    Set.of("ADMIN", "MESERO"),
            new TransitionKey(EstadoLinea.EN_PREPARACION, EstadoLinea.LISTO),
                    Set.of("ADMIN", "MESERO"),
            new TransitionKey(EstadoLinea.LISTO, EstadoLinea.ENTREGADO),
                    Set.of("ADMIN", "MESERO"),
            new TransitionKey(EstadoLinea.PENDIENTE, EstadoLinea.CANCELADO),
                    Set.of("ADMIN", "MESERO"),
            new TransitionKey(EstadoLinea.EN_PREPARACION, EstadoLinea.CANCELADO),
                    Set.of("ADMIN"),
            new TransitionKey(EstadoLinea.LISTO, EstadoLinea.CANCELADO),
                    Set.of("ADMIN")
    );

    private final PedidoRepository pedidoRepository;
    private final LineaPedidoRepository lineaPedidoRepository;
    private final ProductoPort productoPort;

    /**
     * Construye el servicio con sus dependencias.
     *
     * @param pedidoRepository      puerto de salida para pedidos.
     * @param lineaPedidoRepository puerto de salida para líneas de pedido.
     * @param productoPort          puerto de salida para datos de producto.
     */
    public LineaPedidoServiceImpl(final PedidoRepository pedidoRepository,
                                   final LineaPedidoRepository lineaPedidoRepository,
                                   final ProductoPort productoPort) {
        this.pedidoRepository = pedidoRepository;
        this.lineaPedidoRepository = lineaPedidoRepository;
        this.productoPort = productoPort;
    }

    @Override
    public LineaPedido agregarLinea(final Long pedidoId, final Long productoId,
                                    final BigDecimal cantidad, final String notas) {
        final Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new PedidoNotFoundException(pedidoId));
        if (pedido.estado() != com.barquito.pedidos.domain.EstadoPedido.ABIERTO) {
            throw new PedidoOperacionInvalidaException(
                    "Solo se pueden agregar líneas a un pedido ABIERTO. Estado actual: "
                            + pedido.estado());
        }
        final ProductoSnapshot producto = productoPort.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        if (!producto.activo()) {
            throw new PedidoOperacionInvalidaException(
                    "El producto está inactivo y no puede agregarse a un pedido");
        }

        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido nueva = new LineaPedido(null, pedidoId, productoId,
                cantidad, producto.precio(), null,
                EstadoLinea.PENDIENTE, notas, ahora, ahora);
        return lineaPedidoRepository.save(nueva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LineaPedido> listarLineas(final Long pedidoId) {
        return lineaPedidoRepository.findByPedidoId(pedidoId);
    }

    @Override
    @Transactional(readOnly = true)
    public LineaPedido buscarLinea(final Long pedidoId, final Long lineaId) {
        final LineaPedido linea = lineaPedidoRepository.findById(lineaId)
                .orElseThrow(() -> new LineaPedidoNotFoundException(lineaId));
        if (!linea.pedidoId().equals(pedidoId)) {
            throw new LineaPedidoNotFoundException(lineaId);
        }
        return linea;
    }

    @Override
    public LineaPedido actualizarLinea(final Long pedidoId, final Long lineaId,
                                       final BigDecimal cantidad, final String notas) {
        final LineaPedido linea = lineaPedidoRepository.findById(lineaId)
                .orElseThrow(() -> new LineaPedidoNotFoundException(lineaId));
        if (!linea.pedidoId().equals(pedidoId)) {
            throw new LineaPedidoNotFoundException(lineaId);
        }
        if (linea.estado() != EstadoLinea.PENDIENTE) {
            throw new LineaPedidoOperacionInvalidaException(
                    "Solo se pueden actualizar líneas en estado PENDIENTE. Estado actual: "
                            + linea.estado());
        }
        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido actualizada = new LineaPedido(
                linea.id(), linea.pedidoId(), linea.productoId(),
                cantidad != null ? cantidad : linea.cantidad(),
                linea.precioUnitario(), linea.subtotal(),
                linea.estado(),
                notas != null ? notas : linea.notas(),
                linea.creadoEn(), ahora);
        return lineaPedidoRepository.save(actualizada);
    }

    @Override
    public LineaPedido cambiarEstadoLinea(final Long id, final EstadoLinea destino, final String rol) {
        final LineaPedido linea = lineaPedidoRepository.findById(id)
                .orElseThrow(() -> new LineaPedidoNotFoundException(id));

        // Validar que la transición de estado es válida en el dominio
        if (!linea.estado().isTransitionAllowed(destino)) {
            throw new LineaPedidoOperacionInvalidaException(
                    "Transición de estado inválida: " + linea.estado() + " → " + destino);
        }

        // Validar que el rol tiene permiso para esta transición específica
        final TransitionKey key = new TransitionKey(linea.estado(), destino);
        final Set<String> rolesPermitidos = ROLES_PERMITIDOS.get(key);
        if (rolesPermitidos == null || !rolesPermitidos.contains(rol)) {
            throw new AccessDeniedException(
                    "El rol " + rol + " no tiene permiso para la transición "
                            + linea.estado() + " → " + destino);
        }

        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido actualizada = new LineaPedido(
                linea.id(), linea.pedidoId(), linea.productoId(),
                linea.cantidad(), linea.precioUnitario(), linea.subtotal(),
                destino, linea.notas(), linea.creadoEn(), ahora);
        return lineaPedidoRepository.save(actualizada);
    }

    @Override
    public void cancelarLinea(final Long pedidoId, final Long lineaId, final String rol) {
        final LineaPedido linea = lineaPedidoRepository.findById(lineaId)
                .orElseThrow(() -> new LineaPedidoNotFoundException(lineaId));
        if (!linea.pedidoId().equals(pedidoId)) {
            throw new LineaPedidoNotFoundException(lineaId);
        }
        if (linea.estado() != EstadoLinea.PENDIENTE
                && linea.estado() != EstadoLinea.EN_PREPARACION) {
            throw new LineaPedidoOperacionInvalidaException(
                    "Solo se pueden cancelar líneas en estado PENDIENTE o EN_PREPARACION. "
                            + "Estado actual: " + linea.estado());
        }
        if (linea.estado() == EstadoLinea.EN_PREPARACION && !"ADMIN".equals(rol)) {
            throw new AccessDeniedException(
                    "Solo ADMIN puede cancelar una línea EN_PREPARACION");
        }
        final OffsetDateTime ahora = OffsetDateTime.now();
        final LineaPedido cancelada = new LineaPedido(
                linea.id(), linea.pedidoId(), linea.productoId(),
                linea.cantidad(), linea.precioUnitario(), linea.subtotal(),
                EstadoLinea.CANCELADO, linea.notas(), linea.creadoEn(), ahora);
        lineaPedidoRepository.save(cancelada);
    }
}
