package com.barquito.finanzas.api;

import com.barquito.finanzas.application.RegistrarEgresoCommand;
import com.barquito.finanzas.application.ResumenFinanciero;
import com.barquito.finanzas.application.TransaccionResponse;
import com.barquito.finanzas.application.TransaccionService;
import com.barquito.finanzas.application.UsuarioIdResolverPort;
import com.barquito.finanzas.domain.TipoTransaccion;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Controlador REST para el contexto de finanzas.
 *
 * <p>Base path: {@code /api/finanzas}.
 * Todas las operaciones requieren autenticación JWT y rol ADMIN.
 */
@RestController
@RequestMapping("/api/finanzas")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final UsuarioIdResolverPort usuarioIdResolverPort;

    /**
     * Inyección por constructor.
     *
     * @param transaccionService      servicio de transacciones financieras.
     * @param usuarioIdResolverPort   puerto para resolver el id del usuario autenticado.
     */
    public TransaccionController(
            final TransaccionService transaccionService,
            final UsuarioIdResolverPort usuarioIdResolverPort) {
        this.transaccionService = transaccionService;
        this.usuarioIdResolverPort = usuarioIdResolverPort;
    }

    /**
     * Registra un egreso manual. Solo accesible para ADMIN.
     *
     * @param request   body con {@code monto} y {@code descripcion}.
     * @param principal usuario autenticado (extraído del JWT).
     * @return 201 Created con la transacción registrada.
     */
    @PostMapping("/egresos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TransaccionResponse> registrarEgreso(
            @Valid @RequestBody final RegistrarEgresoRequest request,
            final Principal principal) {
        final Long usuarioId = usuarioIdResolverPort.resolverIdPorUsername(principal.getName());
        final RegistrarEgresoCommand command = new RegistrarEgresoCommand(
                request.monto(), request.descripcion(), usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transaccionService.registrarEgreso(command));
    }

    /**
     * Lista transacciones financieras. Solo accesible para ADMIN.
     *
     * <p>Si se proporciona {@code tipo}, filtra por ese tipo (sin paginación).
     * Sin filtro de tipo, aplica paginación con {@code page} y {@code size}.
     *
     * @param tipo filtro opcional de tipo de transacción (INGRESO o EGRESO).
     * @param page número de página (0-based, default 0).
     * @param size tamaño de la página (default 20).
     * @return 200 OK con la lista de transacciones.
     */
    @GetMapping("/transacciones")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TransaccionResponse>> listarTransacciones(
            @RequestParam(required = false) final TipoTransaccion tipo,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "20") final int size) {
        final List<TransaccionResponse> result = tipo != null
                ? transaccionService.listarPorTipo(tipo)
                : transaccionService.listarTransacciones(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Retorna el resumen financiero para un período dado. Solo accesible para ADMIN.
     *
     * @param desde inicio del período (ISO-8601 con offset, e.g. {@code 2026-01-01T00:00:00Z}).
     * @param hasta fin del período (ISO-8601 con offset).
     * @return 200 OK con el resumen financiero.
     */
    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResumenFinanciero> obtenerResumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime hasta) {
        return ResponseEntity.ok(transaccionService.obtenerResumen(desde, hasta));
    }
}
