package com.barquito.finanzas.application;

import java.math.BigDecimal;

/**
 * Comando para registrar un egreso manual en el sistema de finanzas.
 *
 * @param monto       importe del egreso (debe ser positivo).
 * @param descripcion descripción del motivo del egreso.
 * @param usuarioId   identificador del usuario que registra el egreso.
 */
public record RegistrarEgresoCommand(
        BigDecimal monto,
        String descripcion,
        Long usuarioId
) {}
