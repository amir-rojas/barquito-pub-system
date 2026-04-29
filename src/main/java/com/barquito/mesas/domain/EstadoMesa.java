package com.barquito.mesas.domain;

/**
 * Estados posibles de una mesa en el sistema.
 *
 * <p>Los valores se almacenan en la base de datos como texto en mayúsculas.
 * La conversión desde BD se realiza en la capa de infraestructura mediante
 * {@link #fromValue(String)}.
 *
 * <ul>
 *   <li>{@link #DISPONIBLE} — libre, puede recibir clientes.</li>
 *   <li>{@link #OCUPADA} — con clientes activos.</li>
 *   <li>{@link #CUENTA_PEDIDA} — clientes solicitaron la cuenta, pendiente de pago.</li>
 *   <li>{@link #FUSIONADA} — combinada con otra mesa; no acepta pedidos propios.</li>
 * </ul>
 *
 * <p>Transiciones válidas:
 * <pre>
 *   DISPONIBLE ↔ OCUPADA ↔ CUENTA_PEDIDA
 *   DISPONIBLE → FUSIONADA (vía fusionar, solo secundaria)
 *   FUSIONADA → DISPONIBLE (vía desfusionar)
 * </pre>
 * El estado {@code FUSIONADA} solo puede asignarse mediante el caso de uso de fusión;
 * no es un valor válido para {@code cambiarEstado}.
 */
public enum EstadoMesa {

    DISPONIBLE,
    OCUPADA,
    CUENTA_PEDIDA,
    FUSIONADA;

    /**
     * Convierte un valor de texto (de BD) al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor almacenado en la columna {@code estado} de la tabla {@code mesas}.
     * @return el {@link EstadoMesa} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ningún estado definido.
     */
    public static EstadoMesa fromValue(final String value) {
        return EstadoMesa.valueOf(value.toUpperCase());
    }
}
