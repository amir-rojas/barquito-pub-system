package com.barquito.reportes.infrastructure;

import com.barquito.reportes.application.ReporteVentasPort;
import com.barquito.reportes.domain.ResumenVentasData;
import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Adaptador de salida que implementa {@link ReporteVentasPort} usando JPA nativo.
 */
@Component
@Transactional(readOnly = true)
public class ReporteVentasAdapter implements ReporteVentasPort {

    private final ReporteVentasJpaRepository ventasRepo;

    /**
     * Construye el adaptador con el repositorio JPA de ventas.
     *
     * @param ventasRepo repositorio de ventas para reportes.
     */
    public ReporteVentasAdapter(final ReporteVentasJpaRepository ventasRepo) {
        this.ventasRepo = ventasRepo;
    }

    @Override
    public VentasDiariasReporte obtenerVentasDiarias(final LocalDate fecha) {
        final VentasDiariasProjection proj = ventasRepo.findVentasDiarias(fecha);
        return new VentasDiariasReporte(
                fecha,
                (int) proj.getTotalVentas(),
                proj.getMontoTotal(),
                proj.getMontoEfectivo(),
                proj.getMontoQr()
        );
    }

    @Override
    public List<TopProductoItem> obtenerTopProductos(final OffsetDateTime desde,
                                                     final OffsetDateTime hasta,
                                                     final int limit) {
        return ventasRepo.findTopProductos(desde, hasta, limit)
                .stream()
                .map(p -> new TopProductoItem(
                        p.getProductoId(),
                        p.getNombre(),
                        p.getCategoria(),
                        p.getCantidadVendida(),
                        p.getMontoTotal()))
                .toList();
    }

    @Override
    public List<VentasPorCategoriaItem> obtenerVentasPorCategoria(final OffsetDateTime desde,
                                                                  final OffsetDateTime hasta) {
        return ventasRepo.findVentasPorCategoria(desde, hasta)
                .stream()
                .map(p -> new VentasPorCategoriaItem(
                        p.getCategoria(),
                        p.getCantidadVendida(),
                        p.getMontoTotal()))
                .toList();
    }

    @Override
    public ResumenVentasData obtenerResumenVentas(final OffsetDateTime desde, final OffsetDateTime hasta) {
        final ResumenVentasProjection proj = ventasRepo.findResumenVentas(desde, hasta);
        return new ResumenVentasData(proj.getTotalVentas(), proj.getMontoVentas(), proj.getVentasAnuladas());
    }
}
