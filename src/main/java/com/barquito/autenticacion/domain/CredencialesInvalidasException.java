package com.barquito.autenticacion.domain;

/**
 * Excepción de dominio lanzada cuando las credenciales proporcionadas son inválidas.
 *
 * <p>El mensaje es intencionalmente genérico para evitar enumeración de usuarios
 * (el mismo mensaje se usa tanto para usuario inexistente como para PIN incorrecto).
 */
public class CredencialesInvalidasException extends RuntimeException {

    /** Mensaje fijo que nunca revela si el usuario existe o el PIN es incorrecto. */
    private static final String MENSAJE = "Credenciales inválidas";

    /**
     * Crea una nueva instancia con el mensaje genérico de seguridad.
     */
    public CredencialesInvalidasException() {
        super(MENSAJE);
    }
}
