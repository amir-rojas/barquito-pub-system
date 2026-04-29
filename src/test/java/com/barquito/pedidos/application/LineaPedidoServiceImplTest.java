package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.LineaPedidoNotFoundException;
import com.barquito.pedidos.domain.LineaPedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.LineaPedidoRepository;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.pedidos.domain.PedidoNotFoundException;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.PedidoRepository;
import com.barquito.pedidos.domain.ProductoNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LineaPedidoServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class LineaPedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private LineaPedidoRepository lineaPedidoRepository;

    @Mock
    private ProductoPort productoPort;

    @InjectMocks
    private LineaPedidoServiceImpl lineaPedidoService;

    private final OffsetDateTime ahora = OffsetDateTime.now();
    private Pedido pedidoAbierto;
    private Pedido pedidoCerrado;
    private ProductoSnapshot productoActivo;
    private ProductoSnapshot productoInactivo;

    @BeforeEach
    void setUp() {
        pedidoAbierto = new Pedido(1L, 10L, 5L, EstadoPedido.ABIERTO, null, ahora, ahora, null);
        pedidoCerrado = new Pedido(2L, 10L, 5L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);
        productoActivo = new ProductoSnapshot(100L, "Cerveza", new BigDecimal("5.00"), true);
        productoInactivo = new ProductoSnapshot(101L, "Vino", new BigDecimal("8.00"), false);
    }

    private LineaPedido lineaConEstado(final Long id, final Long pedidoId, final EstadoLinea estado) {
        return new LineaPedido(id, pedidoId, 100L,
                new BigDecimal("1.000"), new BigDecimal("5.00"), new BigDecimal("5.00"),
                estado, null, ahora, ahora);
    }

    // =====================================================================
    // agregarLinea
    // =====================================================================

    @Nested
    @DisplayName("agregarLinea()")
    class AgregarLinea {

        @Test
        @DisplayName("happy path — pedido ABIERTO y producto activo crea línea PENDIENTE")
        void agregarLinea_happyPath_creaLineaPendiente() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(productoPort.findById(100L)).thenReturn(Optional.of(productoActivo));
            final LineaPedido lineaGuardada = lineaConEstado(1L, 1L, EstadoLinea.PENDIENTE);
            when(lineaPedidoRepository.save(any())).thenReturn(lineaGuardada);

            final LineaPedido result = lineaPedidoService.agregarLinea(1L, 100L, new BigDecimal("1.000"), null);

            assertThat(result.estado()).isEqualTo(EstadoLinea.PENDIENTE);
            assertThat(result.precioUnitario()).isEqualByComparingTo("5.00");
        }

        @Test
        @DisplayName("pedido no encontrado → PedidoNotFoundException")
        void agregarLinea_pedidoNoEncontrado_lanzaException() {
            when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lineaPedidoService.agregarLinea(99L, 100L, BigDecimal.ONE, null))
                    .isInstanceOf(PedidoNotFoundException.class);
        }

        @Test
        @DisplayName("pedido no ABIERTO → PedidoOperacionInvalidaException")
        void agregarLinea_pedidoNoAbierto_lanzaException() {
            when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedidoCerrado));

            assertThatThrownBy(() -> lineaPedidoService.agregarLinea(2L, 100L, BigDecimal.ONE, null))
                    .isInstanceOf(PedidoOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("producto no encontrado → ProductoNotFoundException")
        void agregarLinea_productoNoEncontrado_lanzaException() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(productoPort.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lineaPedidoService.agregarLinea(1L, 999L, BigDecimal.ONE, null))
                    .isInstanceOf(ProductoNotFoundException.class);
        }

        @Test
        @DisplayName("producto inactivo → PedidoOperacionInvalidaException (409, no 404)")
        void agregarLinea_productoInactivo_lanzaPedidoOperacionInvalidaException() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(productoPort.findById(101L)).thenReturn(Optional.of(productoInactivo));

            assertThatThrownBy(() -> lineaPedidoService.agregarLinea(1L, 101L, BigDecimal.ONE, null))
                    .isInstanceOf(PedidoOperacionInvalidaException.class)
                    .hasMessageContaining("inactivo");
        }
    }

    // =====================================================================
    // cambiarEstadoLinea — transiciones válidas (MESERO/ADMIN hacen todo)
    // =====================================================================

    @Nested
    @DisplayName("cambiarEstadoLinea() — transiciones válidas")
    class CambiarEstadoLineaValido {

        @Test
        @DisplayName("MESERO: PENDIENTE → EN_PREPARACION es válido")
        void mesero_pendiente_enPreparacion_esValido() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.PENDIENTE);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));
            final LineaPedido actualizado = lineaConEstado(1L, 1L, EstadoLinea.EN_PREPARACION);
            when(lineaPedidoRepository.save(any())).thenReturn(actualizado);

            final LineaPedido result = lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.EN_PREPARACION, "MESERO");

            assertThat(result.estado()).isEqualTo(EstadoLinea.EN_PREPARACION);
        }

        @Test
        @DisplayName("ADMIN: EN_PREPARACION → LISTO es válido")
        void admin_enPreparacion_listo_esValido() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.EN_PREPARACION);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));
            final LineaPedido actualizado = lineaConEstado(1L, 1L, EstadoLinea.LISTO);
            when(lineaPedidoRepository.save(any())).thenReturn(actualizado);

            final LineaPedido result = lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.LISTO, "ADMIN");

            assertThat(result.estado()).isEqualTo(EstadoLinea.LISTO);
        }

        @Test
        @DisplayName("MESERO: LISTO → ENTREGADO es válido")
        void mesero_listo_entregado_esValido() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.LISTO);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));
            final LineaPedido actualizado = lineaConEstado(1L, 1L, EstadoLinea.ENTREGADO);
            when(lineaPedidoRepository.save(any())).thenReturn(actualizado);

            final LineaPedido result = lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.ENTREGADO, "MESERO");

            assertThat(result.estado()).isEqualTo(EstadoLinea.ENTREGADO);
        }

        @Test
        @DisplayName("MESERO: PENDIENTE → CANCELADO es válido")
        void mesero_pendiente_cancelado_esValido() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.PENDIENTE);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));
            final LineaPedido actualizado = lineaConEstado(1L, 1L, EstadoLinea.CANCELADO);
            when(lineaPedidoRepository.save(any())).thenReturn(actualizado);

            final LineaPedido result = lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.CANCELADO, "MESERO");

            assertThat(result.estado()).isEqualTo(EstadoLinea.CANCELADO);
        }

        @Test
        @DisplayName("ADMIN: EN_PREPARACION → CANCELADO es válido")
        void admin_enPreparacion_cancelado_esValido() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.EN_PREPARACION);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));
            final LineaPedido actualizado = lineaConEstado(1L, 1L, EstadoLinea.CANCELADO);
            when(lineaPedidoRepository.save(any())).thenReturn(actualizado);

            final LineaPedido result = lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.CANCELADO, "ADMIN");

            assertThat(result.estado()).isEqualTo(EstadoLinea.CANCELADO);
        }
    }

    // =====================================================================
    // cambiarEstadoLinea — transiciones inválidas
    // =====================================================================

    @Nested
    @DisplayName("cambiarEstadoLinea() — transiciones inválidas")
    class CambiarEstadoLineaInvalido {

        @Test
        @DisplayName("transición de estado inválida → LineaPedidoOperacionInvalidaException")
        void transicionInvalida_lanzaException() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.PENDIENTE);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));

            assertThatThrownBy(() -> lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.ENTREGADO, "ADMIN"))
                    .isInstanceOf(LineaPedidoOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("rol sin permiso para la transición → AccessDeniedException")
        void rolSinPermiso_lanzaAccessDeniedException() {
            // MESERO no puede cancelar EN_PREPARACION (solo ADMIN puede)
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.EN_PREPARACION);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));

            assertThatThrownBy(() -> lineaPedidoService.cambiarEstadoLinea(1L, EstadoLinea.CANCELADO, "MESERO"))
                    .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("línea no encontrada → LineaPedidoNotFoundException")
        void lineaNoEncontrada_lanzaException() {
            when(lineaPedidoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lineaPedidoService.cambiarEstadoLinea(99L, EstadoLinea.LISTO, "ADMIN"))
                    .isInstanceOf(LineaPedidoNotFoundException.class);
        }
    }

    // =====================================================================
    // cancelarLinea
    // =====================================================================

    @Nested
    @DisplayName("cancelarLinea()")
    class CancelarLinea {

        @Test
        @DisplayName("PENDIENTE → CANCELADO (204 alias)")
        void cancelarLinea_pendiente_cancelaLinea() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.PENDIENTE);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));
            final LineaPedido cancelada = lineaConEstado(1L, 1L, EstadoLinea.CANCELADO);
            when(lineaPedidoRepository.save(any())).thenReturn(cancelada);

            lineaPedidoService.cancelarLinea(1L, 1L, "MESERO");
        }

        @Test
        @DisplayName("LISTO → no permitido → LineaPedidoOperacionInvalidaException")
        void cancelarLinea_listo_lanzaException() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.LISTO);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));

            assertThatThrownBy(() -> lineaPedidoService.cancelarLinea(1L, 1L, "MESERO"))
                    .isInstanceOf(LineaPedidoOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("ENTREGADO → no permitido → LineaPedidoOperacionInvalidaException")
        void cancelarLinea_entregado_lanzaException() {
            final LineaPedido linea = lineaConEstado(1L, 1L, EstadoLinea.ENTREGADO);
            when(lineaPedidoRepository.findById(1L)).thenReturn(Optional.of(linea));

            assertThatThrownBy(() -> lineaPedidoService.cancelarLinea(1L, 1L, "MESERO"))
                    .isInstanceOf(LineaPedidoOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("línea no encontrada → LineaPedidoNotFoundException")
        void cancelarLinea_noEncontrada_lanzaException() {
            when(lineaPedidoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> lineaPedidoService.cancelarLinea(1L, 99L, "MESERO"))
                    .isInstanceOf(LineaPedidoNotFoundException.class);
        }
    }
}
