package com.barquito.mesas.domain;

/**
 * Entidad de dominio que representa una zona física del local (salón, terraza, barra, etc.).
 *
 * <p>Inmutable por diseño: al ser un record no tiene setters ni estado mutable.
 * No tiene dependencias de frameworks (hexagonal puro).
 *
 * @param id          identificador único de la zona.
 * @param nombre      nombre de la zona (ej. "Salón principal", "Terraza").
 * @param descripcion descripción opcional de la zona; puede ser {@code null}.
 * @param orden       orden de presentación en la interfaz de usuario.
 */
public record Zona(
        Long id,
        String nombre,
        String descripcion,
        int orden
) {}
