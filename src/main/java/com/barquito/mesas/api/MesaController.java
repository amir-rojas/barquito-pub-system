package com.barquito.mesas.api;

import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.FormaMesa;
import com.barquito.mesas.domain.Mesa;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones sobre mesas.
 *
 * <p>Matriz de seguridad:
 * <ul>
 *   <li>GET /api/mesas, GET /api/mesas/{id} — cualquier usuario autenticado.</li>
 *   <li>POST /api/mesas, PUT /api/mesas/{id} — solo ADMIN.</li>
 *   <li>PATCH /api/mesas/{id}/estado — ADMIN o MESERO.</li>
 *   <li>PATCH /api/mesas/{id}/activa — solo ADMIN.</li>
 *   <li>POST /api/mesas/{id}/fusionar, DELETE /api/mesas/{id}/fusionar — ADMIN o MESERO.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    /**
     * Construye el controlador con el servicio de mesas.
     *
     * @param mesaService puerto de entrada para operaciones sobre mesas.
     */
    public MesaController(final MesaService mesaService) {
        this.mesaService = mesaService;
    }

    /**
     * Lista mesas activas, opcionalmente filtradas por zona.
     *
     * @param zonaId id de la zona (opcional); si está ausente retorna todas las mesas activas.
     * @return 200 OK con lista de mesas activas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<List<MesaResponse>> listar(
            @RequestParam(required = false) final Long zonaId) {
        final List<MesaResponse> mesas = (zonaId != null
                ? mesaService.listarMesasPorZona(zonaId)
                : mesaService.listarMesasActivas())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mesas);
    }

    /**
     * Busca una mesa por id.
     *
     * @param id id de la mesa.
     * @return 200 OK con la mesa encontrada.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<MesaResponse> buscarPorId(@PathVariable final Long id) {
        return ResponseEntity.ok(toResponse(mesaService.buscarMesa(id)));
    }

    /**
     * Crea una nueva mesa. Solo ADMIN.
     *
     * @param request datos de la mesa.
     * @return 201 Created con la mesa creada.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MesaResponse> crear(@Valid @RequestBody final CrearMesaRequest request) {
        final FormaMesa forma = request.forma() != null
                ? FormaMesa.fromValue(request.forma()) : null;
        final Mesa mesa = mesaService.crearMesa(request.numero(), request.zonaId(), forma);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(mesa));
    }

    /**
     * Actualiza atributos físicos de una mesa. Solo ADMIN.
     *
     * @param id      id de la mesa.
     * @param request nuevos atributos.
     * @return 200 OK con la mesa actualizada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MesaResponse> actualizar(
            @PathVariable final Long id,
            @Valid @RequestBody final ActualizarMesaRequest request) {
        final FormaMesa forma = request.forma() != null
                ? FormaMesa.fromValue(request.forma()) : null;
        final Mesa mesa = mesaService.actualizarMesa(id, request.numero(), request.zonaId(), forma);
        return ResponseEntity.ok(toResponse(mesa));
    }

    /**
     * Cambia el estado operativo de una mesa. ADMIN o MESERO.
     *
     * @param id      id de la mesa.
     * @param request nuevo estado.
     * @return 200 OK con la mesa actualizada.
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<MesaResponse> cambiarEstado(
            @PathVariable final Long id,
            @Valid @RequestBody final CambiarEstadoRequest request) {
        final EstadoMesa estado = EstadoMesa.fromValue(request.estado());
        final Mesa mesa = mesaService.cambiarEstado(id, estado);
        return ResponseEntity.ok(toResponse(mesa));
    }

    /**
     * Activa o desactiva una mesa. Solo ADMIN.
     *
     * @param id      id de la mesa.
     * @param request nuevo valor de activa.
     * @return 200 OK con la mesa actualizada.
     */
    @PatchMapping("/{id}/activa")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MesaResponse> cambiarActiva(
            @PathVariable final Long id,
            @Valid @RequestBody final CambiarActivaRequest request) {
        final Mesa mesa = mesaService.cambiarActiva(id, request.activa());
        return ResponseEntity.ok(toResponse(mesa));
    }

    /**
     * Fusiona una mesa secundaria bajo esta mesa principal. ADMIN o MESERO.
     *
     * @param id      id de la mesa principal.
     * @param request id de la mesa secundaria.
     * @return 200 OK con la mesa secundaria en estado FUSIONADA.
     */
    @PostMapping("/{id}/fusionar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<MesaResponse> fusionar(
            @PathVariable final Long id,
            @Valid @RequestBody final FusionarMesaRequest request) {
        final Mesa mesa = mesaService.fusionarMesa(id, request.secundariaId());
        return ResponseEntity.ok(toResponse(mesa));
    }

    /**
     * Deshace la fusión de una mesa secundaria. ADMIN o MESERO.
     *
     * @param id id de la mesa secundaria (actualmente FUSIONADA).
     * @return 200 OK con la mesa secundaria en estado DISPONIBLE.
     */
    @PostMapping("/{id}/desfusionar")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<MesaResponse> desfusionar(@PathVariable final Long id) {
        final Mesa mesa = mesaService.desfusionarMesa(id);
        return ResponseEntity.ok(toResponse(mesa));
    }

    private MesaResponse toResponse(final Mesa mesa) {
        return new MesaResponse(
                mesa.id(),
                mesa.numero(),
                mesa.estado().name(),
                mesa.activa(),
                mesa.zonaId(),
                mesa.forma() != null ? mesa.forma().name() : null,
                mesa.mesaPrincipalId()
        );
    }
}
