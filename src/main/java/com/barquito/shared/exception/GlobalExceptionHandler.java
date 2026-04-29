package com.barquito.shared.exception;

import com.barquito.autenticacion.domain.CredencialesInvalidasException;
import com.barquito.caja.domain.VentaNotFoundException;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import com.barquito.mesas.domain.MesaNotFoundException;
import com.barquito.mesas.domain.MesaOperacionInvalidaException;
import com.barquito.mesas.domain.ZonaNotFoundException;
import com.barquito.pedidos.domain.LineaPedidoNotFoundException;
import com.barquito.pedidos.domain.LineaPedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.PedidoNotFoundException;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.ProductoNotFoundException;
import com.barquito.productos.domain.ProductoNombreDuplicadoException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para todos los controladores REST.
 *
 * <p>Centraliza el mapeo de excepciones a respuestas HTTP, evitando que
 * detalles de implementación se filtren al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores de validación de bean (@Valid).
     *
     * @param ex la excepción de validación.
     * @return 400 Bad Request con mapa de errores por campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            final MethodArgumentNotValidException ex) {
        final Map<String, String> errores = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "inválido",
                        (existing, replacement) -> existing));

        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Datos de entrada inválidos", errores));
    }

    /**
     * Maneja parámetros de request requeridos que no fueron provistos — retorna 400.
     *
     * @param ex la excepción de parámetro faltante.
     * @return 400 Bad Request con mensaje descriptivo.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestParam(
            final MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "Parámetro requerido faltante: " + ex.getParameterName(), null));
    }

    /**
     * Maneja parámetros de request con tipo incompatible (ej. texto donde se espera fecha) — retorna 400.
     *
     * @param ex la excepción de tipo incorrecto.
     * @return 400 Bad Request con mensaje descriptivo.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            final MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        "Valor inválido para el parámetro: " + ex.getName(), null));
    }

    /**
     * Maneja cuerpo de request inválido o no legible (ej. enum inválido como "BANANA").
     *
     * <p>Ocurre cuando Jackson no puede deserializar el body JSON, por ejemplo cuando
     * se envía un valor de enum que no existe (e.g., estado="BANANA").
     *
     * @param ex la excepción de deserialización.
     * @return 400 Bad Request con mensaje descriptivo.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            final HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Cuerpo de la petición inválido o no legible", null));
    }

    /**
     * Maneja credenciales inválidas — retorna 401 con mensaje genérico.
     *
     * @param ex la excepción de dominio.
     * @return 401 Unauthorized con mensaje genérico (no revela si el usuario existe).
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredencialesInvalidas(
            final CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja mesa no encontrada — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(MesaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMesaNotFound(final MesaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja zona no encontrada — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(ZonaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleZonaNotFound(final ZonaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja operaciones inválidas sobre mesas — retorna 409.
     *
     * <p>Ejemplos: intentar cambiar estado a FUSIONADA directamente, fusionar una mesa
     * ya fusionada, desactivar una mesa con secundarias activas.
     *
     * @param ex la excepción de dominio.
     * @return 409 Conflict con mensaje descriptivo de la operación rechazada.
     */
    @ExceptionHandler(MesaOperacionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleMesaOperacionInvalida(
            final MesaOperacionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja pedido no encontrado — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(PedidoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePedidoNotFound(final PedidoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja línea de pedido no encontrada — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(LineaPedidoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLineaPedidoNotFound(
            final LineaPedidoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja producto no encontrado o inactivo (módulo pedidos) — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(ProductoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductoNotFound(final ProductoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja producto no encontrado (módulo productos) — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(com.barquito.productos.domain.ProductoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductosProductoNotFound(
            final com.barquito.productos.domain.ProductoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja nombre de producto duplicado — retorna 409.
     *
     * <p>Se lanza cuando se intenta crear o renombrar un producto con un nombre
     * que ya existe en el catálogo (case-insensitive).
     *
     * @param ex la excepción de dominio.
     * @return 409 Conflict con mensaje descriptivo.
     */
    @ExceptionHandler(ProductoNombreDuplicadoException.class)
    public ResponseEntity<ErrorResponse> handleProductoNombreDuplicado(
            final ProductoNombreDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja operaciones inválidas sobre pedidos — retorna 409.
     *
     * @param ex la excepción de dominio.
     * @return 409 Conflict con mensaje descriptivo.
     */
    @ExceptionHandler(PedidoOperacionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handlePedidoOperacionInvalida(
            final PedidoOperacionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja operaciones inválidas sobre líneas de pedido — retorna 409.
     *
     * @param ex la excepción de dominio.
     * @return 409 Conflict con mensaje descriptivo.
     */
    @ExceptionHandler(LineaPedidoOperacionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleLineaPedidoOperacionInvalida(
            final LineaPedidoOperacionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja venta no encontrada — retorna 404.
     *
     * @param ex la excepción de dominio.
     * @return 404 Not Found con mensaje descriptivo.
     */
    @ExceptionHandler(VentaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVentaNotFound(final VentaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja operaciones inválidas sobre ventas — retorna 409.
     *
     * <p>Ejemplos: cobrar una venta ya PAGADA; anular una venta PAGADA; crear venta
     * de un pedido no CERRADO; pedido sin líneas facturables; mesa no en CUENTA_PEDIDA.
     *
     * @param ex la excepción de dominio.
     * @return 409 Conflict con mensaje descriptivo de la operación rechazada.
     */
    @ExceptionHandler(VentaOperacionInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleVentaOperacionInvalida(
            final VentaOperacionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), null));
    }

    /**
     * Maneja acceso denegado lanzado por el servicio en validaciones per-transición.
     *
     * <p>Distinto de {@link AuthorizationDeniedException}: este es lanzado por código
     * de aplicación (ej. rol sin permiso para una transición específica de línea).
     *
     * @param ex la excepción de acceso denegado.
     * @return 403 Forbidden sin detalle adicional.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(final AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Acceso denegado", null));
    }

    /**
     * Maneja acceso denegado por {@code @PreAuthorize} cuando el usuario no tiene el rol requerido.
     *
     * <p>Spring Security 6 lanza {@link AuthorizationDeniedException} (en lugar de
     * {@code AccessDeniedException}) cuando {@code @EnableMethodSecurity} está activo
     * y un método rechaza al usuario autenticado. Este handler lo convierte en 403.
     *
     * @param ex la excepción de autorización denegada.
     * @return 403 Forbidden sin detalle adicional.
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(
            final AuthorizationDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Acceso denegado", null));
    }

    /**
     * Maneja violaciones de constraints de validación en parámetros (@Min, @Max, etc.) — retorna 400.
     *
     * <p>Se lanza cuando @Validated + @Min/@Max fallan en un @RequestParam del controller.
     *
     * @param ex la excepción de constraint.
     * @return 400 Bad Request con mensaje descriptivo.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            final ConstraintViolationException ex) {
        final String mensaje = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .findFirst()
                .orElse("Parámetro inválido");
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(mensaje, null));
    }

    /**
     * Maneja violaciones de integridad de datos de la DB (unicidad, FK) — retorna 409.
     *
     * <p>Protege contra condiciones de carrera: p. ej., dos requests simultáneos
     * creando una venta para el mismo pedido disparan la UNIQUE constraint en pedido_id.
     *
     * @param ex la excepción de integridad.
     * @return 409 Conflict con mensaje genérico.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            final DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Conflicto de datos: la operación viola una restricción de unicidad", null));
    }

    /**
     * Catch-all para excepciones no manejadas.
     *
     * @param ex la excepción inesperada.
     * @return 500 Internal Server Error sin detalle (solo se loguea internamente).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(final Exception ex) {
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error interno del servidor", null));
    }

    /**
     * DTO de respuesta de error.
     *
     * @param message mensaje descriptivo del error.
     * @param errors  mapa de errores por campo (puede ser null si no aplica).
     */
    public record ErrorResponse(String message, Map<String, String> errors) {}
}
