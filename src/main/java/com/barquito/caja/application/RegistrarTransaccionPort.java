package com.barquito.caja.application;

import java.math.BigDecimal;

/**
 * Puerto de salida para registrar una transacción financiera de ingreso.
 *
 * <p>Permite que el contexto caja registre el cobro de una venta como ingreso
 * en el contexto finanzas, sin depender directamente de su implementación.
 *
 * <p>Sigue el patrón establecido en el proyecto: el puerto vive en el módulo
 * que llama (caja), el adaptador implementa la llamada al servicio receptor (finanzas).
 */
public interface RegistrarTransaccionPort {

    /**
     * Registra un ingreso en el sistema de finanzas.
     *
     * @param ventaId     identificador de la venta cobrada.
     * @param monto       importe del cobro.
     * @param descripcion descripción del movimiento (ej. "Cobro venta #1 - EFECTIVO").
     * @param usuarioId   identificador del cajero que realizó el cobro.
     */
    void registrarIngreso(Long ventaId, BigDecimal monto, String descripcion, Long usuarioId);
}
