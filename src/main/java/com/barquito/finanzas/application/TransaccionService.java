package com.barquito.finanzas.application;

import com.barquito.finanzas.domain.TipoTransaccion;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Puerto de entrada (use case) para el módulo de finanzas.
 *
 * <p>Define las operaciones disponibles sobre transacciones financieras.
 */
public interface TransaccionService {

    /**
     * Registra un ingreso asociado a una venta cobrada.
     *
     * @param ventaId     identificador de la venta.
     * @param monto       importe del ingreso (positivo).
     * @param descripcion descripción del cobro.
     * @param usuarioId   identificador del cajero.
     * @return respuesta con la transacción persistida.
     */
    TransaccionResponse registrarIngreso(Long ventaId, BigDecimal monto, String descripcion, Long usuarioId);

    /**
     * Registra un egreso manual (compra de insumos, etc.).
     *
     * @param command comando con los datos del egreso.
     * @return respuesta con la transacción persistida.
     */
    TransaccionResponse registrarEgreso(RegistrarEgresoCommand command);

    /**
     * Lista todas las transacciones ordenadas por fecha descendente.
     *
     * @return lista de transacciones.
     */
    List<TransaccionResponse> listarTransacciones();

    /**
     * Lista transacciones con paginación, ordenadas por fecha descendente.
     *
     * @param page número de página (0-based).
     * @param size tamaño de la página.
     * @return lista de transacciones de la página solicitada.
     */
    List<TransaccionResponse> listarTransacciones(int page, int size);

    /**
     * Lista transacciones filtradas por tipo, ordenadas por fecha descendente.
     *
     * @param tipo tipo de transacción a filtrar.
     * @return lista filtrada de transacciones.
     */
    List<TransaccionResponse> listarPorTipo(TipoTransaccion tipo);

    /**
     * Lista transacciones en un período dado, ordenadas por fecha descendente.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return lista de transacciones en el rango.
     */
    List<TransaccionResponse> listarPorPeriodo(OffsetDateTime desde, OffsetDateTime hasta);

    /**
     * Genera un resumen financiero para el período dado.
     *
     * @param desde inicio del período (inclusive).
     * @param hasta fin del período (inclusive).
     * @return resumen con totales de ingresos, egresos y balance.
     */
    ResumenFinanciero obtenerResumen(OffsetDateTime desde, OffsetDateTime hasta);
}
