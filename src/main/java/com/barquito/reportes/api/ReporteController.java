package com.barquito.reportes.api;

import com.barquito.reportes.application.ReporteService;
import com.barquito.reportes.domain.ResumenPeriodoReporte;
import com.barquito.reportes.domain.TopProductoItem;
import com.barquito.reportes.domain.VentasDiariasReporte;
import com.barquito.reportes.domain.VentasPorCategoriaItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controlador REST para los reportes de negocio.
 *
 * <p>Base path: {@code /api/reportes}.
 * Todos los endpoints requieren rol {@code ADMIN}.
 */
@Validated
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    /**
     * Constructor con inyección por constructor.
     *
     * @param reporteService servicio de reportes.
     */
    public ReporteController(final ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    /**
     * Retorna el reporte de ventas del día indicado.
     *
     * <p>Si no se provee {@code fecha}, se utiliza la fecha actual.
     *
     * @param fecha fecha a consultar en formato {@code yyyy-MM-dd} (opcional).
     * @return 200 OK con el reporte de ventas diarias.
     */
    @GetMapping("/ventas-diarias")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VentasDiariasReporte> ventasDiarias(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate fecha) {
        return ResponseEntity.ok(reporteService.obtenerVentasDiarias(fecha));
    }

    /**
     * Retorna el ranking de productos más vendidos en el período indicado.
     *
     * @param desde inicio del período en ISO-8601.
     * @param hasta fin del período en ISO-8601.
     * @param limit cantidad máxima de ítems (por defecto 10, máximo 50).
     * @return 200 OK con la lista ordenada por monto total descendente.
     */
    @GetMapping("/top-productos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TopProductoItem>> topProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime hasta,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) final int limit) {
        return ResponseEntity.ok(reporteService.obtenerTopProductos(desde, hasta, limit));
    }

    /**
     * Retorna las ventas agrupadas por categoría de producto en el período indicado.
     *
     * @param desde inicio del período en ISO-8601.
     * @param hasta fin del período en ISO-8601.
     * @return 200 OK con la lista ordenada por monto total descendente.
     */
    @GetMapping("/ventas-por-categoria")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VentasPorCategoriaItem>> ventasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime hasta) {
        return ResponseEntity.ok(reporteService.obtenerVentasPorCategoria(desde, hasta));
    }

    /**
     * Retorna el resumen financiero consolidado del período indicado.
     *
     * @param desde inicio del período en ISO-8601.
     * @param hasta fin del período en ISO-8601.
     * @return 200 OK con el resumen del período.
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumenPeriodoReporte> resumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime hasta) {
        return ResponseEntity.ok(reporteService.obtenerResumenPeriodo(desde, hasta));
    }
}
