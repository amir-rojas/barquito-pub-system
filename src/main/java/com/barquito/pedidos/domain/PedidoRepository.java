package com.barquito.pedidos.domain;

import java.util.List;
import java.util.Optional;


/**
 * Puerto de salida (output port) del dominio de pedidos.
 *
 * <p>Define el contrato de acceso a datos para pedidos sin acoplarse a ningún
 * framework de persistencia. La implementación concreta vive en la capa de
 * infraestructura ({@code PedidoJpaAdapter}).
 */
public interface PedidoRepository {

    /**
     * Guarda un pedido (creación o actualización).
     *
     * @param pedido el pedido a persistir.
     * @return el pedido persistido, con el id asignado si es nuevo.
     */
    Pedido save(Pedido pedido);

    /**
     * Busca un pedido por su id.
     *
     * @param id identificador del pedido.
     * @return un {@link Optional} con el pedido si existe, vacío si no.
     */
    Optional<Pedido> findById(Long id);

    /**
     * Retorna todos los pedidos de una mesa.
     *
     * @param mesaId id de la mesa.
     * @return lista de pedidos de la mesa.
     */
    List<Pedido> findByMesaId(Long mesaId);

    /**
     * Cuenta los pedidos ABIERTOS de una mesa.
     *
     * @param mesaId id de la mesa.
     * @return número de pedidos en estado ABIERTO.
     */
    long countAbiertosByMesaId(Long mesaId);

    /**
     * Verifica si existe alguna línea con estado ENTREGADO en cualquier pedido de la mesa.
     *
     * <p>Usado para determinar si al cancelar el último pedido abierto la mesa
     * debe ir a CUENTA_PEDIDA (tuvo consumo) o a DISPONIBLE (no tuvo consumo).
     *
     * @param mesaId id de la mesa.
     * @return {@code true} si existe al menos una línea ENTREGADO en la mesa.
     */
    boolean existsEntregadaLineaByMesaId(Long mesaId);

    /**
     * Busca el pedido ABIERTO de una mesa, si existe.
     *
     * @param mesaId id de la mesa.
     * @return {@link Optional} con el pedido ABIERTO, o vacío si la mesa no tiene pedido activo.
     */
    Optional<Pedido> findAbiertoByMesaId(Long mesaId);

    /**
     * Retorna los pedidos de una mesa filtrando por estado.
     *
     * @param mesaId id de la mesa.
     * @param estado estado a filtrar.
     * @return lista de pedidos en ese estado.
     */
    List<Pedido> findByMesaIdAndEstado(Long mesaId, EstadoPedido estado);
}
