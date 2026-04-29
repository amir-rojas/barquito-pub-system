package com.barquito.finanzas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body para registrar un egreso manual.
 *
 * @param monto       importe del egreso; debe ser positivo y no nulo.
 * @param descripcion descripción del motivo del egreso; no puede estar vacía.
 */
public record RegistrarEgresoRequest(

        @NotNull(message = "El monto es requerido")
        @Positive(message = "El monto debe ser positivo")
        BigDecimal monto,

        @NotBlank(message = "La descripción es requerida")
        String descripcion
) {}
