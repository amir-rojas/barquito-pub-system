package com.barquito.caja.application;

/**
 * Snapshot de un pedido leído por el bounded context de caja.
 *
 * <p>Valor inmutable. Solo contiene los campos que caja necesita para crear
 * y gestionar ventas. No importa tipos del dominio de pedidos.
 *
 * @param id      identificador del pedido.
 * @param mesaId  referencia a la mesa del pedido.
 * @param estado  estado del pedido al momento de la lectura.
 */
public record PedidoSnapshot(
        Long id,
        Long mesaId,
        EstadoPedidoSnapshot estado
) {}
