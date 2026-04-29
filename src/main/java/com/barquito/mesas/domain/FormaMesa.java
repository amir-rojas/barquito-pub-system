package com.barquito.mesas.domain;

/**
 * Formas físicas posibles de una mesa.
 *
 * <p>Los valores se almacenan en la base de datos como texto en mayúsculas.
 * La conversión desde BD se realiza en la capa de infraestructura mediante
 * {@link #fromValue(String)}.
 */
public enum FormaMesa {

    CIRCULAR,
    RECTANGULAR;

    /**
     * Convierte un valor de texto (de BD) al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor almacenado en la columna {@code forma} de la tabla {@code mesas}.
     * @return el {@link FormaMesa} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ninguna forma definida.
     */
    public static FormaMesa fromValue(final String value) {
        return FormaMesa.valueOf(value.toUpperCase());
    }
}
