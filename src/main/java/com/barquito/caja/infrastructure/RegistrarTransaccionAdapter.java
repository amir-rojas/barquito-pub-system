package com.barquito.caja.infrastructure;

import com.barquito.caja.application.RegistrarTransaccionPort;
import com.barquito.finanzas.application.TransaccionService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adaptador de salida que implementa {@link RegistrarTransaccionPort}.
 *
 * <p>Delega el registro del ingreso al servicio del contexto finanzas
 * ({@link TransaccionService}), manteniendo la separación de contextos
 * mediante el patrón de port/adapter.
 */
@Component
public class RegistrarTransaccionAdapter implements RegistrarTransaccionPort {

    private final TransaccionService transaccionService;

    /**
     * Construye el adaptador con el servicio de finanzas.
     *
     * @param transaccionService servicio de transacciones financieras.
     */
    public RegistrarTransaccionAdapter(final TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registrarIngreso(
            final Long ventaId,
            final BigDecimal monto,
            final String descripcion,
            final Long usuarioId) {
        transaccionService.registrarIngreso(ventaId, monto, descripcion, usuarioId);
    }
}
