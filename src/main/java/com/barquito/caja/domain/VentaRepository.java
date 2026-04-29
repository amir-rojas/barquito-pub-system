package com.barquito.caja.domain;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de {@link Venta}.
 *
 * <p>Implementado en la capa de infraestructura. El dominio depende únicamente
 * de esta interfaz (inversión de dependencias).
 */
public interface VentaRepository {

    /**
     * Persiste una venta. Si {@code venta.id()} es {@code null}, realiza INSERT;
     * si no es {@code null}, realiza UPDATE (para transiciones de estado).
     *
     * @param venta la venta a persistir.
     * @return la venta persistida con todos los campos populados (incluyendo id).
     */
    Venta save(Venta venta);

    /**
     * Busca una venta por su identificador único.
     *
     * @param id el identificador de la venta.
     * @return {@link Optional} con la venta si existe, vacío en caso contrario.
     */
    Optional<Venta> findById(Long id);

    /**
     * Busca la venta asociada a un pedido.
     *
     * @param pedidoId el identificador del pedido.
     * @return {@link Optional} con la venta si existe, vacío en caso contrario.
     */
    Optional<Venta> findByPedidoId(Long pedidoId);

    /**
     * Verifica si ya existe una venta para el pedido dado.
     *
     * <p>Se usa como verificación previa a la inserción para proveer un mensaje
     * de error descriptivo antes de que el constraint UNIQUE de la base de datos lo rechace.
     *
     * @param pedidoId el identificador del pedido.
     * @return {@code true} si ya existe una venta para ese pedido.
     */
    boolean existsByPedidoId(Long pedidoId);
}
