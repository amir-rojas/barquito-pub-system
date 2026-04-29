package com.barquito.test.stubs;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador mínimo para el tripwire de método-seguridad.
 *
 * <p>Replica la restricción de acceso del endpoint de creación de mesas.
 * Usa un path diferente ({@code /api/test/mesas}) para evitar conflictos
 * con el {@code MesaController} real.
 *
 * <p>Este bean está en el paquete {@code com.barquito.test.stubs}, fuera
 * del scan del contexto principal ({@code com.barquito.*} sin el subpaquete test).
 * Sólo es activo en slices {@code @WebMvcTest} que lo especifiquen explícitamente.
 */
@RestController
@RequestMapping("/api/test/mesas")
public class SecurityTripwireController {

    /**
     * Endpoint de test para verificar que {@code @EnableMethodSecurity} está activo.
     * Solo ADMIN puede invocar este endpoint.
     *
     * @return OK vacío — nunca debería ejecutarse en el test de tripwire MESERO.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String crearMesa() {
        return "ok";
    }
}
