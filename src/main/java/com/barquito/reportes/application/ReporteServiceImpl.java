package com.barquito.reportes.application;

import com.barquito.reportes.domain.ResumenPeriodoReporte;
import com.barquito.reportes.domain.ResumenVentasData;
import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Implementación del servicio de reportes.
 *
 * <p>Todas las operaciones son de solo lectura ({@code @Transactional(readOnly = true)}).
 * No produce efectos secundarios sobre ningún módulo.
 */
@Service
public class ReporteServiceImpl implements ReporteService {

    /** Límite máximo de ítems para el ranking de top productos. */
    static final int MAX_LIMIT = 50;

    private final ReporteVentasPort ventasPort;
    private final ReporteFinanzasPort finanzasPort;

    /**
     * Constructor con inyección por constructor.
     *
     * @param ventasPort   puerto de salida para reportes de ventas.
     * @param finanzasPort puerto de salida para reportes financieros.
     */
    public ReporteServiceImpl(final ReporteVentasPort ventasPort,
                              final ReporteFinanzasPort finanzasPort) {
        this.ventasPort = ventasPort;
        this.finanzasPort = finanzasPort;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Si {@code fecha} es {@code null}, se utiliza {@link LocalDate#now()}.
     */
    @Override
    @Transactional(readOnly = true)
    public VentasDiariasReporte obtenerVentasDiarias(final LocalDate fecha) {
        final LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        return ventasPort.obtenerVentasDiarias(fechaConsulta);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Si {@code limit} supera {@value #MAX_LIMIT}, se aplica ese máximo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<TopProductoItem> obtenerTopProductos(final OffsetDateTime desde,
                                                     final OffsetDateTime hasta,
                                                     final int limit) {
        return ventasPort.obtenerTopProductos(desde, hasta, Math.min(limit, MAX_LIMIT));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<VentasPorCategoriaItem> obtenerVentasPorCategoria(final OffsetDateTime desde,
                                                                  final OffsetDateTime hasta) {
        return ventasPort.obtenerVentasPorCategoria(desde, hasta);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public ResumenPeriodoReporte obtenerResumenPeriodo(final OffsetDateTime desde,
                                                      final OffsetDateTime hasta) {
        final ResumenVentasData ventasData = ventasPort.obtenerResumenVentas(desde, hasta);
        final BigDecimal totalIngresos = finanzasPort.sumByTipoAndPeriodo("ingreso", desde, hasta);
        final BigDecimal totalEgresos = finanzasPort.sumByTipoAndPeriodo("egreso", desde, hasta);
        final BigDecimal balance = totalIngresos.subtract(totalEgresos);

        return new ResumenPeriodoReporte(
                desde,
                hasta,
                (int) ventasData.totalVentas(),
                ventasData.montoVentas(),
                (int) ventasData.ventasAnuladas(),
                totalIngresos,
                totalEgresos,
                balance
        );
    }
}
