package com.barquito.caja.application;

import com.barquito.caja.domain.DetalleVenta;
import com.barquito.caja.domain.DetalleVentaRepository;
import com.barquito.caja.domain.EstadoVenta;
import com.barquito.caja.domain.MetodoPago;
import com.barquito.caja.domain.Venta;
import com.barquito.caja.domain.VentaNotFoundException;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import com.barquito.caja.domain.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VentaServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @Mock
    private PedidoPort pedidoPort;

    @Mock
    private MesaLiberarPort mesaLiberarPort;

    @Mock
    private UsuarioLookupPort usuarioLookupPort;

    @Mock
    private RegistrarTransaccionPort registrarTransaccionPort;

    @InjectMocks
    private VentaServiceImpl ventaService;

    private final OffsetDateTime ahora = OffsetDateTime.now();

    private Venta ventaPendiente() {
        return new Venta(1L, 10L, 3L, 5L,
                new BigDecimal("22.50"),
                null, EstadoVenta.PENDIENTE,
                ahora, null, null);
    }

    private Venta ventaPagada() {
        return new Venta(2L, 10L, 3L, 5L,
                new BigDecimal("22.50"),
                MetodoPago.EFECTIVO, EstadoVenta.PAGADA,
                ahora, ahora, null);
    }

    private Venta ventaAnulada() {
        return new Venta(3L, 10L, 3L, 5L,
                new BigDecimal("22.50"),
                null, EstadoVenta.ANULADA,
                ahora, null, ahora);
    }

    private PedidoSnapshot pedidoCerrado() {
        return new PedidoSnapshot(10L, 3L, EstadoPedidoSnapshot.CERRADO);
    }

    private LineaPedidoSnapshot lineaEntregada(final Long id, final BigDecimal precio) {
        return new LineaPedidoSnapshot(id, 100L, "Producto " + id,
                new BigDecimal("1.000"), precio,
                precio, EstadoLineaSnapshot.ENTREGADO);
    }

    private LineaPedidoSnapshot lineaCancelada(final Long id) {
        return new LineaPedidoSnapshot(id, 200L, "Cancelado",
                new BigDecimal("1.000"), new BigDecimal("5.00"),
                new BigDecimal("5.00"), EstadoLineaSnapshot.CANCELADO);
    }

    private DetalleVenta detalleSaved(final Long id, final Long ventaId) {
        return new DetalleVenta(id, ventaId, 100L, "Producto",
                new BigDecimal("1.000"), new BigDecimal("10.00"),
                new BigDecimal("10.00"));
    }

    // =====================================================================
    // crearVenta
    // =====================================================================

    @Nested
    @DisplayName("crearVenta()")
    class CrearVenta {

        @Test
        @DisplayName("happy path — crea venta PENDIENTE con total y detalles correctos")
        void crearVenta_happyPath_creaVentaConDetalles() {
            when(usuarioLookupPort.resolverIdPorUsername("cajero1")).thenReturn(5L);
            when(pedidoPort.findSnapshot(10L)).thenReturn(Optional.of(pedidoCerrado()));
            when(ventaRepository.existsByPedidoId(10L)).thenReturn(false);
            when(pedidoPort.findLineasByPedidoId(10L)).thenReturn(List.of(
                    lineaEntregada(1L, new BigDecimal("10.00")),
                    lineaEntregada(2L, new BigDecimal("12.50"))
            ));
            final Venta saved = new Venta(1L, 10L, 3L, 5L,
                    new BigDecimal("22.50"), null, EstadoVenta.PENDIENTE, ahora, null, null);
            when(ventaRepository.save(any())).thenReturn(saved);
            when(detalleVentaRepository.saveAll(any())).thenReturn(List.of(
                    detalleSaved(1L, 1L), detalleSaved(2L, 1L)
            ));

            final VentaConDetalles result = ventaService.crearVenta(10L, "cajero1");

            assertThat(result.venta().estado()).isEqualTo(EstadoVenta.PENDIENTE);
            assertThat(result.venta().total()).isEqualByComparingTo(new BigDecimal("22.50"));
            assertThat(result.detalles()).hasSize(2);
        }

        @Test
        @DisplayName("pedido no encontrado → VentaNotFoundException (404)")
        void crearVenta_pedidoNoEncontrado_lanzaException() {
            when(usuarioLookupPort.resolverIdPorUsername("cajero1")).thenReturn(5L);
            when(pedidoPort.findSnapshot(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ventaService.crearVenta(999L, "cajero1"))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("pedido ABIERTO → VentaOperacionInvalidaException")
        void crearVenta_pedidoAbierto_lanzaException() {
            when(usuarioLookupPort.resolverIdPorUsername("cajero1")).thenReturn(5L);
            when(pedidoPort.findSnapshot(10L)).thenReturn(Optional.of(
                    new PedidoSnapshot(10L, 3L, EstadoPedidoSnapshot.ABIERTO)
            ));

            assertThatThrownBy(() -> ventaService.crearVenta(10L, "cajero1"))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("pedido CANCELADO → VentaOperacionInvalidaException")
        void crearVenta_pedidoCancelado_lanzaException() {
            when(usuarioLookupPort.resolverIdPorUsername("cajero1")).thenReturn(5L);
            when(pedidoPort.findSnapshot(10L)).thenReturn(Optional.of(
                    new PedidoSnapshot(10L, 3L, EstadoPedidoSnapshot.CANCELADO)
            ));

            assertThatThrownBy(() -> ventaService.crearVenta(10L, "cajero1"))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("venta ya existe para el pedido → VentaOperacionInvalidaException")
        void crearVenta_ventaYaExiste_lanzaException() {
            when(usuarioLookupPort.resolverIdPorUsername("cajero1")).thenReturn(5L);
            when(pedidoPort.findSnapshot(10L)).thenReturn(Optional.of(pedidoCerrado()));
            when(ventaRepository.existsByPedidoId(10L)).thenReturn(true);

            assertThatThrownBy(() -> ventaService.crearVenta(10L, "cajero1"))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("todas las líneas CANCELADO → VentaOperacionInvalidaException")
        void crearVenta_todasLineasCanceladas_lanzaException() {
            when(usuarioLookupPort.resolverIdPorUsername("cajero1")).thenReturn(5L);
            when(pedidoPort.findSnapshot(10L)).thenReturn(Optional.of(pedidoCerrado()));
            when(ventaRepository.existsByPedidoId(10L)).thenReturn(false);
            when(pedidoPort.findLineasByPedidoId(10L)).thenReturn(List.of(
                    lineaCancelada(1L), lineaCancelada(2L)
            ));

            assertThatThrownBy(() -> ventaService.crearVenta(10L, "cajero1"))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }
    }

    // =====================================================================
    // cobrarVenta
    // =====================================================================

    @Nested
    @DisplayName("cobrarVenta()")
    class CobrarVenta {

        @Test
        @DisplayName("happy path — PENDIENTE→PAGADA y libera mesa exactamente una vez")
        void cobrarVenta_happyPath_pagadaYLiberaMesa() {
            final Venta pendiente = ventaPendiente();
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(pendiente));
            final Venta saved = new Venta(1L, 10L, 3L, 5L,
                    new BigDecimal("22.50"), MetodoPago.EFECTIVO, EstadoVenta.PAGADA,
                    ahora, ahora, null);
            when(ventaRepository.save(any())).thenReturn(saved);
            when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of(detalleSaved(1L, 1L)));

            final VentaConDetalles result = ventaService.cobrarVenta(1L, MetodoPago.EFECTIVO);

            assertThat(result.venta().estado()).isEqualTo(EstadoVenta.PAGADA);
            verify(mesaLiberarPort).liberarMesa(3L);
        }

        @Test
        @DisplayName("después del cobro, registrarTransaccionPort.registrarIngreso es llamado una vez con datos correctos")
        void cobrarVenta_happyPath_registraIngreso() {
            final Venta pendiente = ventaPendiente();
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(pendiente));
            final Venta saved = new Venta(1L, 10L, 3L, 5L,
                    new BigDecimal("22.50"), MetodoPago.EFECTIVO, EstadoVenta.PAGADA,
                    ahora, ahora, null);
            when(ventaRepository.save(any())).thenReturn(saved);
            when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of(detalleSaved(1L, 1L)));

            ventaService.cobrarVenta(1L, MetodoPago.EFECTIVO);

            verify(registrarTransaccionPort).registrarIngreso(
                    eq(1L),
                    eq(new BigDecimal("22.50")),
                    eq("Cobro venta #1 - EFECTIVO"),
                    eq(5L)
            );
        }

        @Test
        @DisplayName("venta no encontrada → VentaNotFoundException")
        void cobrarVenta_noEncontrada_lanzaException() {
            when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ventaService.cobrarVenta(999L, MetodoPago.EFECTIVO))
                    .isInstanceOf(VentaNotFoundException.class);
        }

        @Test
        @DisplayName("venta PAGADA → VentaOperacionInvalidaException, no llama liberarMesa")
        void cobrarVenta_pagada_lanzaExceptionYNoLiberaMesa() {
            when(ventaRepository.findById(2L)).thenReturn(Optional.of(ventaPagada()));

            assertThatThrownBy(() -> ventaService.cobrarVenta(2L, MetodoPago.EFECTIVO))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
            verify(mesaLiberarPort, never()).liberarMesa(any());
        }

        @Test
        @DisplayName("venta ANULADA → VentaOperacionInvalidaException")
        void cobrarVenta_anulada_lanzaException() {
            when(ventaRepository.findById(3L)).thenReturn(Optional.of(ventaAnulada()));

            assertThatThrownBy(() -> ventaService.cobrarVenta(3L, MetodoPago.EFECTIVO))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }
    }

    // =====================================================================
    // anularVenta
    // =====================================================================

    @Nested
    @DisplayName("anularVenta()")
    class AnularVenta {

        @Test
        @DisplayName("happy path — PENDIENTE→ANULADA, NO se llama mesaLiberarPort")
        void anularVenta_happyPath_anuladaYNoLiberaMesa() {
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaPendiente()));
            final Venta saved = new Venta(1L, 10L, 3L, 5L,
                    new BigDecimal("22.50"), null, EstadoVenta.ANULADA, ahora, null, ahora);
            when(ventaRepository.save(any())).thenReturn(saved);
            when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of());

            final VentaConDetalles result = ventaService.anularVenta(1L);

            assertThat(result.venta().estado()).isEqualTo(EstadoVenta.ANULADA);
            verify(mesaLiberarPort, never()).liberarMesa(any());
        }

        @Test
        @DisplayName("venta no encontrada → VentaNotFoundException")
        void anularVenta_noEncontrada_lanzaException() {
            when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ventaService.anularVenta(999L))
                    .isInstanceOf(VentaNotFoundException.class);
        }

        @Test
        @DisplayName("venta PAGADA → VentaOperacionInvalidaException")
        void anularVenta_pagada_lanzaException() {
            when(ventaRepository.findById(2L)).thenReturn(Optional.of(ventaPagada()));

            assertThatThrownBy(() -> ventaService.anularVenta(2L))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("venta ANULADA → VentaOperacionInvalidaException")
        void anularVenta_anulada_lanzaException() {
            when(ventaRepository.findById(3L)).thenReturn(Optional.of(ventaAnulada()));

            assertThatThrownBy(() -> ventaService.anularVenta(3L))
                    .isInstanceOf(VentaOperacionInvalidaException.class);
        }
    }

    // =====================================================================
    // buscarVenta
    // =====================================================================

    @Nested
    @DisplayName("buscarVenta()")
    class BuscarVenta {

        @Test
        @DisplayName("encontrada → retorna VentaConDetalles")
        void buscarVenta_encontrada_retornaConDetalles() {
            when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaPendiente()));
            when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of(detalleSaved(1L, 1L)));

            final VentaConDetalles result = ventaService.buscarVenta(1L);

            assertThat(result.venta().id()).isEqualTo(1L);
            assertThat(result.detalles()).hasSize(1);
        }

        @Test
        @DisplayName("no encontrada → VentaNotFoundException")
        void buscarVenta_noEncontrada_lanzaException() {
            when(ventaRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ventaService.buscarVenta(999L))
                    .isInstanceOf(VentaNotFoundException.class);
        }
    }

    // =====================================================================
    // buscarPorPedido
    // =====================================================================

    @Nested
    @DisplayName("buscarPorPedido()")
    class BuscarPorPedido {

        @Test
        @DisplayName("venta encontrada por pedidoId → retorna Optional.of(VentaConDetalles)")
        void buscarPorPedido_encontrada_retornaOptionalConValor() {
            when(ventaRepository.findByPedidoId(10L)).thenReturn(Optional.of(ventaPendiente()));
            when(detalleVentaRepository.findByVentaId(1L)).thenReturn(List.of(detalleSaved(1L, 1L)));

            final Optional<VentaConDetalles> result = ventaService.buscarPorPedido(10L);

            assertThat(result).isPresent();
            assertThat(result.get().venta().pedidoId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("no existe venta para el pedido → retorna Optional.empty()")
        void buscarPorPedido_noEncontrada_retornaEmpty() {
            when(ventaRepository.findByPedidoId(999L)).thenReturn(Optional.empty());

            final Optional<VentaConDetalles> result = ventaService.buscarPorPedido(999L);

            assertThat(result).isEmpty();
        }
    }
}
