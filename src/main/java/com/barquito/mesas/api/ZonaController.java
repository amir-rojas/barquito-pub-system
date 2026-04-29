package com.barquito.mesas.api;

import com.barquito.mesas.application.ZonaService;
import com.barquito.mesas.domain.Zona;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones sobre zonas.
 *
 * <p>Seguridad:
 * <ul>
 *   <li>GET /api/zonas — cualquier usuario autenticado.</li>
 *   <li>POST /api/zonas — solo ADMIN.</li>
 *   <li>PUT /api/zonas/{id} — solo ADMIN.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/zonas")
public class ZonaController {

    private final ZonaService zonaService;

    /**
     * Construye el controlador con el servicio de zonas.
     *
     * @param zonaService puerto de entrada para operaciones sobre zonas.
     */
    public ZonaController(final ZonaService zonaService) {
        this.zonaService = zonaService;
    }

    /**
     * Lista todas las zonas ordenadas.
     *
     * @return 200 OK con lista de zonas.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MESERO')")
    public ResponseEntity<List<ZonaResponse>> listar() {
        final List<ZonaResponse> zonas = zonaService.listarZonas()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(zonas);
    }

    /**
     * Crea una nueva zona. Solo ADMIN.
     *
     * @param request datos de la zona a crear.
     * @return 201 Created con la zona creada.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZonaResponse> crear(@Valid @RequestBody final CrearZonaRequest request) {
        final Zona zona = zonaService.crearZona(
                request.nombre(), request.descripcion(), request.orden());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(zona));
    }

    /**
     * Actualiza una zona existente. Solo ADMIN.
     *
     * @param id      id de la zona.
     * @param request nuevos datos.
     * @return 200 OK con la zona actualizada.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ZonaResponse> actualizar(
            @PathVariable final Long id,
            @Valid @RequestBody final ActualizarZonaRequest request) {
        final Zona zona = zonaService.actualizarZona(
                id, request.nombre(), request.descripcion(), request.orden());
        return ResponseEntity.ok(toResponse(zona));
    }

    private ZonaResponse toResponse(final Zona zona) {
        return new ZonaResponse(zona.id(), zona.nombre(), zona.descripcion(), zona.orden());
    }
}
