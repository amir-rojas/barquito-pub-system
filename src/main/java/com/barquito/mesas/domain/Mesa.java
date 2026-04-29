package com.barquito.mesas.domain;

/**
 * Entidad de dominio que representa una mesa del local.
 *
 * <p>Inmutable por diseño: al ser un record no tiene setters ni estado mutable.
 * Todas las transiciones producen una nueva instancia mediante los métodos {@code con*()}.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * <p>Invariantes de fusión:
 * <ul>
 *   <li>Una mesa puede ser principal si está {@link EstadoMesa#DISPONIBLE} o
 *       {@link EstadoMesa#OCUPADA} y NO es ella misma secundaria de otra fusión.</li>
 *   <li>Una mesa puede ser secundaria si está {@link EstadoMesa#DISPONIBLE} y
 *       no tiene ya un {@code mesaPrincipalId} asignado (no está ya fusionada).</li>
 *   <li>El estado {@link EstadoMesa#FUSIONADA} solo se asigna a secundarias mediante
 *       el caso de uso de fusión; no es un destino válido para {@code cambiarEstado}.</li>
 * </ul>
 *
 * @param id               identificador único de la mesa.
 * @param numero           identificador visible de la mesa (ej. "1", "A3", "Barra-1").
 * @param estado           estado operativo actual de la mesa.
 * @param activa           indica si la mesa está habilitada para operar.
 * @param zonaId           referencia a la {@link Zona} a la que pertenece la mesa.
 * @param forma            forma física de la mesa; puede ser {@code null} si no se especificó.
 * @param mesaPrincipalId  id de la mesa principal si esta es una secundaria fusionada;
 *                         {@code null} si no es secundaria.
 */
public record Mesa(
        Long id,
        String numero,
        EstadoMesa estado,
        boolean activa,
        Long zonaId,
        FormaMesa forma,
        Long mesaPrincipalId
) {

    /**
     * Indica si esta mesa está fusionada como secundaria de otra.
     *
     * @return {@code true} si {@code mesaPrincipalId} tiene valor (no es null).
     */
    public boolean esFusionada() {
        return mesaPrincipalId != null;
    }

    /**
     * Indica si esta mesa puede actuar como mesa principal en una fusión.
     *
     * <p>Una mesa puede ser principal cuando:
     * <ul>
     *   <li>Su estado es {@link EstadoMesa#DISPONIBLE} o {@link EstadoMesa#OCUPADA}.</li>
     *   <li>No es ella misma secundaria de otra fusión ({@code mesaPrincipalId == null}).</li>
     * </ul>
     *
     * @return {@code true} si puede ser principal de una fusión.
     */
    public boolean puedeSerPrincipal() {
        return !esFusionada()
                && (estado == EstadoMesa.DISPONIBLE || estado == EstadoMesa.OCUPADA);
    }

    /**
     * Indica si esta mesa puede fusionarse como secundaria bajo otra mesa principal.
     *
     * <p>Una mesa puede ser secundaria cuando:
     * <ul>
     *   <li>Su estado es {@link EstadoMesa#DISPONIBLE}.</li>
     *   <li>No tiene ya una mesa principal asignada (no está ya fusionada).</li>
     * </ul>
     *
     * @return {@code true} si puede ser secundaria de una fusión.
     */
    public boolean puedeSerFusionadaComoSecundaria() {
        return !esFusionada() && estado == EstadoMesa.DISPONIBLE;
    }

    /**
     * Produce una nueva instancia de esta mesa con el estado dado.
     *
     * @param nuevoEstado el nuevo estado a aplicar.
     * @return nueva instancia con el estado actualizado.
     */
    public Mesa conEstado(final EstadoMesa nuevoEstado) {
        return new Mesa(id, numero, nuevoEstado, activa, zonaId, forma, mesaPrincipalId);
    }

    /**
     * Produce una nueva instancia de esta mesa con el valor de {@code activa} dado.
     *
     * @param nuevaActiva nuevo valor de habilitación.
     * @return nueva instancia con el campo {@code activa} actualizado.
     */
    public Mesa conActiva(final boolean nuevaActiva) {
        return new Mesa(id, numero, estado, nuevaActiva, zonaId, forma, mesaPrincipalId);
    }

    /**
     * Produce una nueva instancia con los atributos físicos actualizados.
     *
     * @param nuevoNumero nuevo número/identificador visible; {@code null} para no cambiar.
     * @param nuevaForma  nueva forma física; {@code null} para no cambiar.
     * @param nuevoZonaId nuevo id de zona; {@code null} para no cambiar.
     * @return nueva instancia con los atributos físicos actualizados.
     */
    public Mesa conAtributosFisicos(final String nuevoNumero,
                                    final FormaMesa nuevaForma,
                                    final Long nuevoZonaId) {
        return new Mesa(
                id,
                nuevoNumero != null ? nuevoNumero : numero,
                estado,
                activa,
                nuevoZonaId != null ? nuevoZonaId : zonaId,
                nuevaForma != null ? nuevaForma : forma,
                mesaPrincipalId
        );
    }
}
