package com.barquito.reportes.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Puerto de salida para consultas financieras de reportes.
 *
 * <p>Aísla la capa de aplicación de la implementación JPA concreta.
 */
public interface ReporteFinanzasPort {

    /**
     * Retorna la suma de transacciones de un tipo en el período indicado.
     *
     * @param tipo  tipo de transacción ("ingreso" o "egreso").
     * @param desde inicio del período.
     * @param hasta fin del período.
     * @return suma de montos, o {@link BigDecimal#ZERO} si no hay transacciones.
     */
    BigDecimal sumByTipoAndPeriodo(String tipo, OffsetDateTime desde, OffsetDateTime hasta);
}
