package com.barquito.autenticacion.domain;

/**
 * Roles disponibles en el sistema.
 *
 * <p>Los valores están en minúsculas en la base de datos; se convierten a enum
 * al momento de mapear desde la capa de infraestructura.
 */
public enum Rol {

    ADMIN,
    MESERO,
    BARTENDER;

    /**
     * Convierte un valor de texto (de BD) al enum correspondiente, sin distinguir mayúsculas.
     *
     * @param value el valor almacenado en la columna {@code rol} de la tabla {@code usuarios}.
     * @return el {@link Rol} correspondiente.
     * @throws IllegalArgumentException si el valor no coincide con ningún rol definido.
     */
    public static Rol fromValue(final String value) {
        return Rol.valueOf(value.toUpperCase());
    }
}
