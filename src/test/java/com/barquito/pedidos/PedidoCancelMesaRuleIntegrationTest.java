package com.barquito.pedidos;

import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.pedidos.application.LineaPedidoService;
import com.barquito.pedidos.application.PedidoService;
import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.productos.infrastructure.ProductoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the cancelarPedido mesa rule (both branches).
 *
 * <p>Branch A: no ENTREGADO lines → mesa → DISPONIBLE.
 * <p>Branch B: has ENTREGADO lines → mesa → CUENTA_PEDIDA.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class PedidoCancelMesaRuleIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private LineaPedidoService lineaPedidoService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private com.barquito.mesas.infrastructure.MesaJpaRepository mesaJpaRepository;

    @Autowired
    private com.barquito.mesas.infrastructure.ZonaJpaRepository zonaJpaRepository;

    @Autowired
    private com.barquito.autenticacion.infrastructure.UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private ProductoJpaRepository productoJpaRepository;

    private Long mesaId;
    private String nombreMesero;
    private Long productoId;

    @BeforeEach
    void setUp() {
        mesaId = mesaJpaRepository.findAll().stream()
                .filter(m -> "DISPONIBLE".equals(m.getEstado()) && m.isActiva())
                .findFirst().map(m -> m.getId()).orElse(null);

        nombreMesero = usuarioJpaRepository.findAll().stream()
                .filter(u -> "mesero".equals(u.getRol()) && u.isActivo())
                .findFirst().map(u -> u.getNombre()).orElse(null);

        productoId = productoJpaRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .findFirst().map(p -> p.getId()).orElse(null);
    }

    @Test
    @DisplayName("Branch A: cancelar sin líneas ENTREGADO → mesa DISPONIBLE")
    void cancelar_sinEntregados_mesaDisponible() {
        if (mesaId == null || nombreMesero == null) {
            return;
        }

        final Pedido pedido = pedidoService.crearPedido(mesaId, nombreMesero, null);
        assertThat(mesaService.buscarMesa(mesaId).estado()).isEqualTo(EstadoMesa.OCUPADA);

        final Pedido cancelado = pedidoService.cancelarPedido(pedido.id());

        assertThat(cancelado.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(mesaService.buscarMesa(mesaId).estado()).isEqualTo(EstadoMesa.DISPONIBLE);
    }

    @Test
    @DisplayName("Branch B: cancelar con líneas ENTREGADO → mesa CUENTA_PEDIDA")
    void cancelar_conEntregados_mesaCuentaPedida() {
        if (mesaId == null || nombreMesero == null || productoId == null) {
            return;
        }

        // Reset mesa to DISPONIBLE first by re-fetching
        mesaService.cambiarEstado(mesaId, EstadoMesa.DISPONIBLE);

        final Pedido pedido = pedidoService.crearPedido(mesaId, nombreMesero, null);

        // Add a line and deliver it
        final var linea = lineaPedidoService.agregarLinea(
                pedido.id(), productoId, new BigDecimal("1.000"), null);
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.EN_PREPARACION, "MESERO");
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.LISTO, "MESERO");
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.ENTREGADO, "MESERO");

        final Pedido cancelado = pedidoService.cancelarPedido(pedido.id());

        assertThat(cancelado.estado()).isEqualTo(EstadoPedido.CANCELADO);
        assertThat(mesaService.buscarMesa(mesaId).estado()).isEqualTo(EstadoMesa.CUENTA_PEDIDA);
    }
}
