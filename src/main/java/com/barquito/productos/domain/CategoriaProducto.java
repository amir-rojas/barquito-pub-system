package com.barquito.productos.domain;

/**
 * Categoría de un producto del catálogo.
 *
 * <p>Se almacena como TEXT en la base de datos con CHECK constraint.
 * No se usa un ENUM de PostgreSQL para mantener flexibilidad en migraciones futuras.
 */
public enum CategoriaProducto {
    /** Cervezas de todo tipo (artesanal, industrial, importada). */
    CERVEZA,

    /** Destilados y licores (whisky, ron, vodka, gin, fernet, etc.). */
    ESPIRITUOSO,

    /** Gaseosas, aguas y bebidas sin alcohol. */
    GASEOSA,

    /** Categoría genérica para productos que no encajan en las anteriores. */
    OTRO
}
