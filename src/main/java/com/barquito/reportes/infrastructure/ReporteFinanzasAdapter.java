package com.barquito.reportes.infrastructure;

import com.barquito.reportes.application.ReporteFinanzasPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Adaptador de salida que implementa {@link ReporteFinanzasPort} usando JPA nativo.
 */
@Component
@Transactional(readOnly = true)
public class ReporteFinanzasAdapter implements ReporteFinanzasPort {

    private final ReporteFinanzasJpaRepository finanzasRepo;

    /**
     * Construye el adaptador con el repositorio JPA de finanzas.
     *
     * @param finanzasRepo repositorio de finanzas para reportes.
     */
    public ReporteFinanzasAdapter(final ReporteFinanzasJpaRepository finanzasRepo) {
        this.finanzasRepo = finanzasRepo;
    }

    @Override
    public BigDecimal sumByTipoAndPeriodo(final String tipo, final OffsetDateTime desde,
                                          final OffsetDateTime hasta) {
        return Objects.requireNonNullElse(
                finanzasRepo.sumByTipoAndPeriodo(tipo, desde, hasta), BigDecimal.ZERO);
    }
}
