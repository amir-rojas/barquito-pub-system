package com.barquito.caja.application;

import com.barquito.caja.domain.MetodoPago;

import java.util.Optional;

/**
 * Puerto de entrada para las operaciones del contexto caja.
 *
 * <p>Define el contrato público del servicio de ventas, implementado por
 * {@link VentaServiceImpl}.
 */
public interface VentaService {

    /**
     * Crea una {@code Venta} en estado PENDIENTE desde un pedido CERRADO.
     *
     * @param pedidoId       identificador del pedido origen.
     * @param cajeroUsername nombre de usuario del cajero (extraído del JWT).
     * @return la venta creada junto con sus detalles.
     * @throws com.barquito.caja.domain.VentaOperacionInvalidaException si el pedido
     *         no existe, no está CERRADO, ya tiene una venta, o no tiene líneas facturables.
     */
    VentaConDetalles crearVenta(Long pedidoId, String cajeroUsername);

    /**
     * Transiciona una venta de PENDIENTE a PAGADA y libera la mesa de forma atómica.
     *
     * @param ventaId    identificador de la venta.
     * @param metodoPago método de pago utilizado.
     * @return la venta cobrada junto con sus detalles.
     * @throws com.barquito.caja.domain.VentaNotFoundException           si la venta no existe.
     * @throws com.barquito.caja.domain.VentaOperacionInvalidaException  si la venta no está PENDIENTE.
     */
    VentaConDetalles cobrarVenta(Long ventaId, MetodoPago metodoPago);

    /**
     * Transiciona una venta de PENDIENTE a ANULADA. La mesa no se modifica.
     *
     * @param ventaId identificador de la venta.
     * @return la venta anulada junto con sus detalles.
     * @throws com.barquito.caja.domain.VentaNotFoundException           si la venta no existe.
     * @throws com.barquito.caja.domain.VentaOperacionInvalidaException  si la venta no está PENDIENTE.
     */
    VentaConDetalles anularVenta(Long ventaId);

    /**
     * Retorna una venta por su identificador junto con sus detalles.
     *
     * @param ventaId identificador de la venta.
     * @return la venta con sus detalles.
     * @throws com.barquito.caja.domain.VentaNotFoundException si la venta no existe.
     */
    VentaConDetalles buscarVenta(Long ventaId);

    /**
     * Retorna la venta asociada a un pedido, si existe.
     *
     * @param pedidoId identificador del pedido.
     * @return {@link Optional} con la venta si existe, vacío en caso contrario.
     */
    Optional<VentaConDetalles> buscarPorPedido(Long pedidoId);
}
