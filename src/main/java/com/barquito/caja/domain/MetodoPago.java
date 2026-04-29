package com.barquito.caja.domain;

/**
 * Métodos de pago aceptados al cobrar una venta.
 *
 * <p>Los valores se almacenan en la base de datos como texto en mayúsculas.
 * La conversión se realiza mediante {@link #fromValue(String)}.
 *
 * <ul>
 *   <li>{@link #EFECTIVO} — pago en efectivo.</li>
 *   <li>{@link #QR} — pago mediante código QR (transferencia).</li>
 * </ul>
 */
public enum MetodoPago {

    EFECTIVO,
    QR;

    /**
     * Convierte un valor de texto al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor a convertir.
     * @return el {@link MetodoPago} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ningún método de pago.
     */
    public static MetodoPago fromValue(final String value) {
        return MetodoPago.valueOf(value.toUpperCase());
    }
}
