package com.barquito.caja.domain;

/**
 * Estados posibles de una venta.
 *
 * <p>Los valores se almacenan en la base de datos como texto en mayúsculas.
 * La conversión se realiza en la capa de infraestructura mediante {@link #fromValue(String)}.
 *
 * <ul>
 *   <li>{@link #PENDIENTE} — venta creada, pendiente de cobro.</li>
 *   <li>{@link #PAGADA} — venta cobrada con método de pago registrado.</li>
 *   <li>{@link #ANULADA} — venta anulada antes de ser cobrada.</li>
 * </ul>
 *
 * <p>Transiciones válidas:
 * <pre>
 *   PENDIENTE → PAGADA
 *   PENDIENTE → ANULADA
 * </pre>
 * PAGADA y ANULADA son estados terminales.
 */
public enum EstadoVenta {

    PENDIENTE,
    PAGADA,
    ANULADA;

    /**
     * Convierte un valor de texto al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor almacenado en la base de datos.
     * @return el {@link EstadoVenta} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ningún estado.
     */
    public static EstadoVenta fromValue(final String value) {
        return EstadoVenta.valueOf(value.toUpperCase());
    }

    /**
     * Indica si la transición al estado dado está permitida desde este estado.
     *
     * @param destino el estado destino.
     * @return {@code true} si la transición está permitida.
     */
    public boolean isTransitionAllowed(final EstadoVenta destino) {
        return switch (this) {
            case PENDIENTE -> destino == PAGADA || destino == ANULADA;
            case PAGADA, ANULADA -> false;
        };
    }
}
