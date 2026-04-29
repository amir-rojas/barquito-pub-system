package com.barquito.pedidos.application;

import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.LineaPedido;
import com.barquito.pedidos.domain.LineaPedidoRepository;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.pedidos.domain.PedidoNotFoundException;
import com.barquito.pedidos.domain.PedidoOperacionInvalidaException;
import com.barquito.pedidos.domain.PedidoRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PedidoServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private LineaPedidoRepository lineaPedidoRepository;

    @Mock
    private MesaStatusPort mesaStatusPort;

    @Mock
    private UsuarioLookupPort usuarioLookupPort;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private final OffsetDateTime ahora = OffsetDateTime.now();

    private Pedido pedidoAbierto;
    private Pedido pedidoCerrado;
    private Pedido pedidoCancelado;

    @BeforeEach
    void setUp() {
        pedidoAbierto = new Pedido(1L, 10L, 5L, EstadoPedido.ABIERTO, null, ahora, ahora, null);
        pedidoCerrado = new Pedido(2L, 10L, 5L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);
        pedidoCancelado = new Pedido(3L, 10L, 5L, EstadoPedido.CANCELADO, null, ahora, ahora, ahora);
    }

    private LineaPedido lineaConEstado(final Long id, final EstadoLinea estado) {
        return new LineaPedido(id, 1L, 100L,
                new BigDecimal("1.000"), new BigDecimal("10.00"), new BigDecimal("10.00"),
                estado, null, ahora, ahora);
    }

    // =====================================================================
    // crearPedido
    // =====================================================================

    @Nested
    @DisplayName("crearPedido()")
    class CrearPedido {

        @Test
        @DisplayName("happy path — mesa DISPONIBLE crea pedido ABIERTO y llama ocupar")
        void crearPedido_mesaDisponible_creaPedidoYOcupaMesa() {
            when(usuarioLookupPort.findIdByNombre("mesero1")).thenReturn(5L);
            when(pedidoRepository.save(any())).thenReturn(pedidoAbierto);

            final Pedido result = pedidoService.crearPedido(10L, "mesero1", null);

            assertThat(result.estado()).isEqualTo(EstadoPedido.ABIERTO);
            verify(mesaStatusPort).ocupar(10L);
        }

        @Test
        @DisplayName("mesa FUSIONADA → PedidoOperacionInvalidaException")
        void crearPedido_mesaFusionada_lanzaException() {
            when(usuarioLookupPort.findIdByNombre("mesero1")).thenReturn(5L);
            // MesaStatusPort.ocupar lanza la excepción cuando la mesa es FUSIONADA
            org.mockito.Mockito.doThrow(new PedidoOperacionInvalidaException("Mesa FUSIONADA"))
                    .when(mesaStatusPort).ocupar(10L);

            assertThatThrownBy(() -> pedidoService.crearPedido(10L, "mesero1", null))
                    .isInstanceOf(PedidoOperacionInvalidaException.class);
        }
    }

    // =====================================================================
    // listarPedidos
    // =====================================================================

    @Nested
    @DisplayName("listarPedidos()")
    class ListarPedidos {

        @Test
        @DisplayName("listarByMesa retorna pedidos de la mesa")
        void listarByMesa_retornaPedidos() {
            when(pedidoRepository.findByMesaId(10L)).thenReturn(List.of(pedidoAbierto));

            final List<Pedido> result = pedidoService.listarPedidosByMesa(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).mesaId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("listarByMesa sin pedidos retorna lista vacía")
        void listarByMesa_sinPedidos_retornaVacia() {
            when(pedidoRepository.findByMesaId(99L)).thenReturn(List.of());

            final List<Pedido> result = pedidoService.listarPedidosByMesa(99L);

            assertThat(result).isEmpty();
        }
    }

    // =====================================================================
    // cerrarPedido
    // =====================================================================

    @Nested
    @DisplayName("cerrarPedido()")
    class CerrarPedido {

        @Test
        @DisplayName("último pedido abierto de mesa → estado CERRADO y llama transicionarACuentaPedida")
        void cerrarPedido_ultimoAbierto_cerradoYCuentaPedida() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(pedidoRepository.countAbiertosByMesaId(10L)).thenReturn(0L); // after close
            final Pedido cerrado = new Pedido(1L, 10L, 5L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);
            when(pedidoRepository.save(any())).thenReturn(cerrado);

            final Pedido result = pedidoService.cerrarPedido(1L);

            assertThat(result.estado()).isEqualTo(EstadoPedido.CERRADO);
            verify(mesaStatusPort).transicionarACuentaPedida(10L);
        }

        @Test
        @DisplayName("no es el último pedido abierto → no llama transicionarACuentaPedida")
        void cerrarPedido_noUltimoAbierto_noLlamaCuentaPedida() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(pedidoRepository.countAbiertosByMesaId(10L)).thenReturn(1L); // still has open
            final Pedido cerrado = new Pedido(1L, 10L, 5L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);
            when(pedidoRepository.save(any())).thenReturn(cerrado);

            pedidoService.cerrarPedido(1L);

            verify(mesaStatusPort, never()).transicionarACuentaPedida(any());
        }

        @Test
        @DisplayName("pedido no encontrado → PedidoNotFoundException")
        void cerrarPedido_noEncontrado_lanzaException() {
            when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pedidoService.cerrarPedido(99L))
                    .isInstanceOf(PedidoNotFoundException.class);
        }

        @Test
        @DisplayName("pedido no ABIERTO → PedidoOperacionInvalidaException")
        void cerrarPedido_noAbierto_lanzaException() {
            when(pedidoRepository.findById(2L)).thenReturn(Optional.of(pedidoCerrado));

            assertThatThrownBy(() -> pedidoService.cerrarPedido(2L))
                    .isInstanceOf(PedidoOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("cerrarPedido con líneas activas (no ENTREGADO/CANCELADO) → PedidoOperacionInvalidaException")
        void cerrarPedido_conLineasActivas_lanzaException() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(lineaPedidoRepository.findByPedidoId(1L)).thenReturn(List.of(
                    lineaConEstado(1L, EstadoLinea.PENDIENTE),
                    lineaConEstado(2L, EstadoLinea.ENTREGADO)
            ));

            assertThatThrownBy(() -> pedidoService.cerrarPedido(1L))
                    .isInstanceOf(PedidoOperacionInvalidaException.class)
                    .hasMessageContaining("hay líneas sin entregar");
        }

        @Test
        @DisplayName("cerrarPedido con líneas EN_PREPARACION → PedidoOperacionInvalidaException")
        void cerrarPedido_conLineasEnPreparacion_lanzaException() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(lineaPedidoRepository.findByPedidoId(1L)).thenReturn(List.of(
                    lineaConEstado(1L, EstadoLinea.EN_PREPARACION)
            ));

            assertThatThrownBy(() -> pedidoService.cerrarPedido(1L))
                    .isInstanceOf(PedidoOperacionInvalidaException.class)
                    .hasMessageContaining("hay líneas sin entregar");
        }

        @Test
        @DisplayName("cerrarPedido con todas las líneas ENTREGADO o CANCELADO → cierra correctamente")
        void cerrarPedido_todasLineasEntregadasOCanceladas_cierraCorrectamente() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(lineaPedidoRepository.findByPedidoId(1L)).thenReturn(List.of(
                    lineaConEstado(1L, EstadoLinea.ENTREGADO),
                    lineaConEstado(2L, EstadoLinea.CANCELADO)
            ));
            when(pedidoRepository.countAbiertosByMesaId(10L)).thenReturn(0L);
            final Pedido cerrado = new Pedido(1L, 10L, 5L, EstadoPedido.CERRADO, null, ahora, ahora, ahora);
            when(pedidoRepository.save(any())).thenReturn(cerrado);

            final Pedido result = pedidoService.cerrarPedido(1L);

            assertThat(result.estado()).isEqualTo(EstadoPedido.CERRADO);
        }
    }

    // =====================================================================
    // cancelarPedido
    // =====================================================================

    @Nested
    @DisplayName("cancelarPedido()")
    class CancelarPedido {

        @Test
        @DisplayName("sin líneas ENTREGADO y sin más pedidos → libera mesa (DISPONIBLE)")
        void cancelarPedido_sinEntregados_liberaMesa() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            when(lineaPedidoRepository.findByPedidoId(1L)).thenReturn(List.of(
                    lineaConEstado(1L, EstadoLinea.PENDIENTE)
            ));
            when(pedidoRepository.countAbiertosByMesaId(10L)).thenReturn(0L);
            when(pedidoRepository.existsEntregadaLineaByMesaId(10L)).thenReturn(false);
            final Pedido cancelado = new Pedido(1L, 10L, 5L, EstadoPedido.CANCELADO, null, ahora, ahora, ahora);
            when(pedidoRepository.save(any())).thenReturn(cancelado);
            when(lineaPedidoRepository.saveAll(any())).thenReturn(List.of());

            pedidoService.cancelarPedido(1L);

            verify(mesaStatusPort).liberarMesa(10L);
        }

        @Test
        @DisplayName("con líneas ENTREGADO y sin más pedidos → transiciona mesa a CUENTA_PEDIDA")
        void cancelarPedido_conEntregados_cuentaPedida() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            // ENTREGADO lines are NOT cancelled (filter keeps only non-CANCELADO/non-ENTREGADO)
            // so saveAll won't be called — don't stub it
            when(lineaPedidoRepository.findByPedidoId(1L)).thenReturn(List.of(
                    lineaConEstado(1L, EstadoLinea.ENTREGADO)
            ));
            when(pedidoRepository.countAbiertosByMesaId(10L)).thenReturn(0L);
            when(pedidoRepository.existsEntregadaLineaByMesaId(10L)).thenReturn(true);
            final Pedido cancelado = new Pedido(1L, 10L, 5L, EstadoPedido.CANCELADO, null, ahora, ahora, ahora);
            when(pedidoRepository.save(any())).thenReturn(cancelado);

            pedidoService.cancelarPedido(1L);

            verify(mesaStatusPort).transicionarACuentaPedida(10L);
        }

        @Test
        @DisplayName("aún hay pedidos abiertos en la mesa → mesa permanece OCUPADA (no se llama al port)")
        void cancelarPedido_otrosPedidosAbiertos_noLlamaMesaPort() {
            when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoAbierto));
            // empty lineas → saveAll not called
            when(lineaPedidoRepository.findByPedidoId(1L)).thenReturn(List.of());
            when(pedidoRepository.countAbiertosByMesaId(10L)).thenReturn(1L);
            final Pedido cancelado = new Pedido(1L, 10L, 5L, EstadoPedido.CANCELADO, null, ahora, ahora, ahora);
            when(pedidoRepository.save(any())).thenReturn(cancelado);

            pedidoService.cancelarPedido(1L);

            verify(mesaStatusPort, never()).liberarMesa(any());
            verify(mesaStatusPort, never()).transicionarACuentaPedida(any());
        }

        @Test
        @DisplayName("pedido no encontrado → PedidoNotFoundException")
        void cancelarPedido_noEncontrado_lanzaException() {
            when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pedidoService.cancelarPedido(99L))
                    .isInstanceOf(PedidoNotFoundException.class);
        }
    }
}
