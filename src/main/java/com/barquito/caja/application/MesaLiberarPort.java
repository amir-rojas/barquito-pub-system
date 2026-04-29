package com.barquito.caja.application;

/**
 * Puerto de salida para liberar una mesa al cobrar una venta.
 *
 * <p>El adaptador debe ejecutarse dentro de la transacción del caller
 * ({@code Propagation.MANDATORY}).
 */
public interface MesaLiberarPort {

    /**
     * Adquiere bloqueo pesimista sobre la fila de la mesa y la transiciona
     * de CUENTA_PEDIDA a DISPONIBLE.
     *
     * <p>Requiere una transacción activa — el adaptador usa {@code Propagation.MANDATORY}.
     *
     * @param mesaId identificador de la mesa a liberar.
     * @throws com.barquito.caja.domain.VentaOperacionInvalidaException si la mesa
     *         no se encuentra en estado CUENTA_PEDIDA.
     */
    void liberarMesa(Long mesaId);
}
