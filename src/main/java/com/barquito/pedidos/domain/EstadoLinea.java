package com.barquito.pedidos.domain;

/**
 * Estados posibles de una línea de pedido.
 *
 * <p>Los valores se almacenan en la base de datos como texto en mayúsculas.
 * La conversión se realiza en la capa de infraestructura mediante {@link #fromValue(String)}.
 *
 * <ul>
 *   <li>{@link #PENDIENTE} — recién creada, esperando preparación.</li>
 *   <li>{@link #EN_PREPARACION} — en proceso de elaboración.</li>
 *   <li>{@link #LISTO} — preparada, esperando entrega.</li>
 *   <li>{@link #ENTREGADO} — entregada al cliente (terminal).</li>
 *   <li>{@link #CANCELADO} — anulada (terminal).</li>
 * </ul>
 *
 * <p>Transiciones válidas:
 * <pre>
 *   PENDIENTE → EN_PREPARACION
 *   PENDIENTE → CANCELADO
 *   EN_PREPARACION → LISTO
 *   EN_PREPARACION → CANCELADO
 *   LISTO → ENTREGADO
 *   LISTO → CANCELADO
 * </pre>
 * ENTREGADO y CANCELADO son estados terminales.
 */
public enum EstadoLinea {

    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    /**
     * Convierte un valor de texto al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor almacenado en la base de datos.
     * @return el {@link EstadoLinea} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ningún estado.
     */
    public static EstadoLinea fromValue(final String value) {
        return EstadoLinea.valueOf(value.toUpperCase());
    }

    /**
     * Indica si la transición al estado dado está permitida desde este estado.
     *
     * @param destino el estado destino.
     * @return {@code true} si la transición está permitida.
     */
    public boolean isTransitionAllowed(final EstadoLinea destino) {
        return switch (this) {
            case PENDIENTE -> destino == EN_PREPARACION || destino == CANCELADO;
            case EN_PREPARACION -> destino == LISTO || destino == CANCELADO;
            case LISTO -> destino == ENTREGADO || destino == CANCELADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
