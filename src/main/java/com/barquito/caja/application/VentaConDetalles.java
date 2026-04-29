package com.barquito.caja.application;

import com.barquito.caja.domain.DetalleVenta;
import com.barquito.caja.domain.Venta;

import java.util.List;

/**
 * Proyección que agrupa una {@link Venta} con sus {@link DetalleVenta} para
 * retornar como resultado de las operaciones del servicio.
 *
 * <p>Espejo del patrón {@code PedidoConLineas} del contexto pedidos.
 *
 * @param venta    la venta principal.
 * @param detalles los detalles asociados a la venta.
 */
public record VentaConDetalles(Venta venta, List<DetalleVenta> detalles) {}
