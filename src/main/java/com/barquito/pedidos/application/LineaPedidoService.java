package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.LineaPedido;

import java.math.BigDecimal;
import java.util.List;

/**
 * Puerto de entrada (input port) para operaciones sobre líneas de pedido.
 *
 * <p>La implementación concreta es {@link LineaPedidoServiceImpl}.
 */
public interface LineaPedidoService {

    /**
     * Agrega una línea a un pedido ABIERTO.
     *
     * <p>Captura el precio del producto como snapshot en el momento de la creación.
     *
     * @param pedidoId   id del pedido.
     * @param productoId id del producto.
     * @param cantidad   cantidad pedida.
     * @param notas      notas opcionales.
     * @return la línea creada en estado PENDIENTE.
     */
    LineaPedido agregarLinea(Long pedidoId, Long productoId, BigDecimal cantidad, String notas);

    /**
     * Lista las líneas de un pedido.
     *
     * @param pedidoId id del pedido.
     * @return lista de líneas.
     */
    List<LineaPedido> listarLineas(Long pedidoId);

    /**
     * Busca una línea por su id, validando que pertenece al pedido indicado.
     *
     * @param pedidoId id del pedido esperado.
     * @param lineaId  identificador de la línea.
     * @return la línea encontrada.
     * @throws com.barquito.pedidos.domain.LineaPedidoNotFoundException si no existe
     *         o no pertenece al pedido.
     */
    LineaPedido buscarLinea(Long pedidoId, Long lineaId);

    /**
     * Actualiza cantidad y/o notas de una línea PENDIENTE, validando que pertenece al pedido.
     *
     * @param pedidoId id del pedido al que debe pertenecer la línea.
     * @param lineaId  id de la línea.
     * @param cantidad nueva cantidad (puede ser null para no cambiar).
     * @param notas    nuevas notas (puede ser null para no cambiar).
     * @return la línea actualizada.
     */
    LineaPedido actualizarLinea(Long pedidoId, Long lineaId, BigDecimal cantidad, String notas);

    /**
     * Cambia el estado de una línea verificando la transición y el rol del usuario.
     *
     * @param id      id de la línea.
     * @param destino estado destino.
     * @param rol     rol del usuario que realiza la acción (ej. "ADMIN", "MESERO").
     * @return la línea con el nuevo estado.
     */
    LineaPedido cambiarEstadoLinea(Long id, EstadoLinea destino, String rol);

    /**
     * Cancela una línea PENDIENTE o EN_PREPARACION (DELETE 204).
     *
     * <p>La transición EN_PREPARACION → CANCELADO requiere rol ADMIN.
     *
     * @param pedidoId id del pedido al que debe pertenecer la línea.
     * @param lineaId  id de la línea.
     * @param rol      rol del usuario que realiza la acción (ej. "ADMIN", "MESERO").
     */
    void cancelarLinea(Long pedidoId, Long lineaId, String rol);
}
