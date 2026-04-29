package com.barquito.finanzas.application;

import com.barquito.finanzas.domain.TipoTransaccion;
import com.barquito.finanzas.domain.Transaccion;
import com.barquito.finanzas.domain.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementación del puerto de entrada {@link TransaccionService}.
 *
 * <p>Coordina el puerto de salida {@link TransaccionRepository} para orquestar
 * el registro y consulta de transacciones financieras.
 */
@Service
@Transactional(readOnly = true)
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;

    /**
     * Inyección por constructor.
     *
     * @param transaccionRepository repositorio de transacciones.
     */
    public TransaccionServiceImpl(final TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransaccionResponse registrarIngreso(
            final Long ventaId,
            final BigDecimal monto,
            final String descripcion,
            final Long usuarioId) {
        final Transaccion transaccion = new Transaccion(
                null, TipoTransaccion.INGRESO, monto, descripcion, ventaId, usuarioId, null);
        return TransaccionResponse.from(transaccionRepository.save(transaccion));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public TransaccionResponse registrarEgreso(final RegistrarEgresoCommand command) {
        final Transaccion transaccion = new Transaccion(
                null, TipoTransaccion.EGRESO, command.monto(), command.descripcion(),
                null, command.usuarioId(), null);
        return TransaccionResponse.from(transaccionRepository.save(transaccion));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarTransacciones() {
        return transaccionRepository.findAllOrderByFechaHoraDesc().stream()
                .map(TransaccionResponse::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarTransacciones(final int page, final int size) {
        return transaccionRepository.findAllOrderByFechaHoraDescPaged(page, size).stream()
                .map(TransaccionResponse::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarPorTipo(final TipoTransaccion tipo) {
        return transaccionRepository.findAllByTipoOrderByFechaHoraDesc(tipo).stream()
                .map(TransaccionResponse::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransaccionResponse> listarPorPeriodo(
            final OffsetDateTime desde, final OffsetDateTime hasta) {
        return transaccionRepository.findAllByFechaHoraBetweenOrderByFechaHoraDesc(desde, hasta).stream()
                .map(TransaccionResponse::from)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public ResumenFinanciero obtenerResumen(final OffsetDateTime desde, final OffsetDateTime hasta) {
        final BigDecimal totalIngresos = transaccionRepository
                .sumMontoByTipoAndFechaHoraBetween(TipoTransaccion.INGRESO, desde, hasta);
        final BigDecimal totalEgresos = transaccionRepository
                .sumMontoByTipoAndFechaHoraBetween(TipoTransaccion.EGRESO, desde, hasta);
        final BigDecimal balance = totalIngresos.subtract(totalEgresos);
        return new ResumenFinanciero(totalIngresos, totalEgresos, balance, desde, hasta);
    }
}
