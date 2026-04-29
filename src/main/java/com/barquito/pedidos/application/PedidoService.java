package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.Pedido;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada (input port) para operaciones sobre pedidos.
 *
 * <p>Define el contrato que expone la capa de aplicación al mundo exterior.
 * La implementación concreta es {@link PedidoServiceImpl}.
 */
public interface PedidoService {

    /**
     * Crea un nuevo pedido para una mesa.
     *
     * <p>Adquiere PESSIMISTIC_WRITE sobre la mesa y la transiciona a OCUPADA.
     * Lanza excepción si la mesa está FUSIONADA o inactiva.
     *
     * @param mesaId       id de la mesa.
     * @param nombreMesero nombre del mesero (subject del JWT).
     * @param notas        notas opcionales.
     * @return el pedido creado en estado ABIERTO.
     */
    Pedido crearPedido(Long mesaId, String nombreMesero, String notas);

    /**
     * Lista todos los pedidos de una mesa.
     *
     * @param mesaId id de la mesa.
     * @return lista de pedidos.
     */
    List<Pedido> listarPedidosByMesa(Long mesaId);

    /**
     * Busca un pedido por su id, incluyendo sus líneas.
     *
     * @param id identificador del pedido.
     * @return pedido con líneas.
     * @throws com.barquito.pedidos.domain.PedidoNotFoundException si no existe.
     */
    PedidoConLineas buscarPedidoConLineas(Long id);

    /**
     * Actualiza las notas de un pedido ABIERTO.
     *
     * @param id    id del pedido.
     * @param notas nuevas notas.
     * @return el pedido actualizado.
     */
    Pedido actualizarNotas(Long id, String notas);

    /**
     * Cierra un pedido y, si es el último abierto de la mesa, transiciona la mesa a CUENTA_PEDIDA.
     *
     * @param id id del pedido.
     * @return el pedido en estado CERRADO.
     */
    Pedido cerrarPedido(Long id);

    /**
     * Cancela un pedido y aplica la regla de mesa correspondiente.
     *
     * @param id id del pedido.
     * @return el pedido en estado CANCELADO.
     */
    Pedido cancelarPedido(Long id);

    /**
     * Busca el pedido ABIERTO de una mesa, incluyendo sus líneas.
     *
     * @param mesaId id de la mesa.
     * @return {@link Optional} con el pedido activo y sus líneas, o vacío si no hay pedido abierto.
     */
    Optional<PedidoConLineas> buscarPedidoActivoPorMesa(Long mesaId);

    /**
     * Lista los pedidos ABIERTOS de una mesa (para uso POS).
     *
     * @param mesaId id de la mesa.
     * @return lista de pedidos en estado ABIERTO (0 o 1 elemento por invariante de dominio).
     */
    List<Pedido> listarPedidosAbiertosByMesa(Long mesaId);
}
