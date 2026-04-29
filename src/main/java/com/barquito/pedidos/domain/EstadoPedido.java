package com.barquito.pedidos.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados posibles de un pedido.
 *
 * <p>Los valores se almacenan en la base de datos como texto en mayúsculas.
 * La conversión se realiza en la capa de infraestructura mediante {@link #fromValue(String)}.
 *
 * <ul>
 *   <li>{@link #ABIERTO} — pedido activo, acepta líneas.</li>
 *   <li>{@link #CERRADO} — pedido finalizado con cuenta presentada.</li>
 *   <li>{@link #CANCELADO} — pedido anulado.</li>
 * </ul>
 *
 * <p>Transiciones válidas:
 * <pre>
 *   ABIERTO → CERRADO
 *   ABIERTO → CANCELADO
 * </pre>
 * CERRADO y CANCELADO son estados terminales.
 */
public enum EstadoPedido {

    ABIERTO,
    CERRADO,
    CANCELADO;

    /**
     * Convierte un valor de texto al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor almacenado en la base de datos.
     * @return el {@link EstadoPedido} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ningún estado.
     */
    public static EstadoPedido fromValue(final String value) {
        return EstadoPedido.valueOf(value.toUpperCase());
    }

    /**
     * Indica si la transición al estado dado está permitida desde este estado.
     *
     * @param destino el estado destino.
     * @return {@code true} si la transición está permitida.
     */
    public boolean isTransitionAllowed(final EstadoPedido destino) {
        return switch (this) {
            case ABIERTO -> destino == CERRADO || destino == CANCELADO;
            case CERRADO, CANCELADO -> false;
        };
    }
}
