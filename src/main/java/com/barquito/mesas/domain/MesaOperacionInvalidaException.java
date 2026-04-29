package com.barquito.mesas.domain;

/**
 * Excepción lanzada cuando se intenta realizar una operación inválida sobre una mesa.
 *
 * <p>Casos de uso que la lanzan:
 * <ul>
 *   <li>Cambiar estado a {@link EstadoMesa#FUSIONADA} directamente (debe usarse el endpoint de fusión).</li>
 *   <li>Fusionar una mesa que ya está fusionada o en estado no apto.</li>
 *   <li>Desactivar una mesa que tiene secundarias activas.</li>
 *   <li>Desactivar una mesa que no está en estado {@link EstadoMesa#DISPONIBLE}.</li>
 *   <li>Fusionar una mesa consigo misma.</li>
 *   <li>Fusionar generando un ciclo (A→B cuando B ya apunta a A).</li>
 * </ul>
 *
 * <p>El manejador global ({@code GlobalExceptionHandler}) la convierte en una
 * respuesta HTTP 409 Conflict.
 */
public class MesaOperacionInvalidaException extends RuntimeException {

    /**
     * Construye la excepción con un mensaje descriptivo de la operación rechazada.
     *
     * @param mensaje descripción de por qué la operación es inválida.
     */
    public MesaOperacionInvalidaException(final String mensaje) {
        super(mensaje);
    }
}
