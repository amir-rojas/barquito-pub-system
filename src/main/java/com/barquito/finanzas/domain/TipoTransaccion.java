package com.barquito.finanzas.domain;

/**
 * Tipos de transacciones financieras del sistema.
 *
 * <p>Mapeado a la columna {@code tipo} de tipo {@code tipo_transaccion} (PG ENUM).
 * Los valores en base de datos están en minúsculas: {@code ingreso} y {@code egreso}.
 */
public enum TipoTransaccion {

    /** Entrada de dinero al establecimiento (ej. cobro de venta). */
    INGRESO,

    /** Salida de dinero del establecimiento (ej. compra de insumos). */
    EGRESO
}
