package com.barquito.pedidos.domain;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (output port) del dominio de líneas de pedido.
 *
 * <p>Define el contrato de acceso a datos para líneas sin acoplarse a ningún
 * framework de persistencia.
 */
public interface LineaPedidoRepository {

    /**
     * Guarda una línea de pedido (creación o actualización).
     *
     * @param linea la línea a persistir.
     * @return la línea persistida, con id y subtotal asignados.
     */
    LineaPedido save(LineaPedido linea);

    /**
     * Busca una línea por su id.
     *
     * @param id identificador de la línea.
     * @return un {@link Optional} con la línea si existe, vacío si no.
     */
    Optional<LineaPedido> findById(Long id);

    /**
     * Retorna todas las líneas de un pedido.
     *
     * @param pedidoId id del pedido.
     * @return lista de líneas del pedido.
     */
    List<LineaPedido> findByPedidoId(Long pedidoId);

    /**
     * Guarda múltiples líneas de pedido en lote.
     *
     * @param lineas lista de líneas a persistir.
     * @return lista de líneas persistidas.
     */
    List<LineaPedido> saveAll(List<LineaPedido> lineas);
}
