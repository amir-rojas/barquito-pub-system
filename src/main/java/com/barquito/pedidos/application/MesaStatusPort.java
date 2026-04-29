package com.barquito.pedidos.application;

/**
 * Puerto de salida para transiciones de estado de mesa coordinadas por pedidos.
 *
 * <p>Todos los métodos requieren una transacción activa del llamador
 * ({@code Propagation.MANDATORY}), ya que adquieren un lock PESSIMISTIC_WRITE
 * sobre la fila de mesa.
 */
public interface MesaStatusPort {

    /**
     * Adquiere lock PESSIMISTIC_WRITE sobre la mesa y la transiciona a OCUPADA.
     *
     * <p>Lanza {@link com.barquito.pedidos.domain.PedidoOperacionInvalidaException}
     * si la mesa está en estado inválido para ser ocupada (ej. FUSIONADA).
     *
     * @param mesaId id de la mesa a ocupar.
     */
    void ocupar(Long mesaId);

    /**
     * Transiciona la mesa al estado CUENTA_PEDIDA.
     *
     * @param mesaId id de la mesa.
     */
    void transicionarACuentaPedida(Long mesaId);

    /**
     * Libera la mesa transicionándola a DISPONIBLE.
     *
     * @param mesaId id de la mesa.
     */
    void liberarMesa(Long mesaId);
}
