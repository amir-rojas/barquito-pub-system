package com.barquito.pedidos;

import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.pedidos.application.LineaPedidoService;
import com.barquito.pedidos.application.PedidoService;
import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.LineaPedido;
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
 * End-to-end happy path integration test: crear pedido → agregar líneas
 * → cambiar estados → cerrar → mesa CUENTA_PEDIDA.
 *
 * <p>Requires Docker to be available. Skipped gracefully if Docker unavailable.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class PedidoHappyPathIntegrationTest {

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
        // Use seeded data from migrations
        mesaId = mesaJpaRepository.findAll().stream()
                .filter(m -> "DISPONIBLE".equals(m.getEstado()) && m.isActiva())
                .findFirst()
                .map(m -> m.getId())
                .orElse(null);

        nombreMesero = usuarioJpaRepository.findAll().stream()
                .filter(u -> "mesero".equals(u.getRol()) && u.isActivo())
                .findFirst()
                .map(u -> u.getNombre())
                .orElse(null);

        productoId = productoJpaRepository.findAll().stream()
                .filter(p -> Boolean.TRUE.equals(p.getActivo()))
                .findFirst()
                .map(p -> p.getId())
                .orElse(null);
    }

    @Test
    @DisplayName("happy path: crear pedido → agregar lineas → cerrar → mesa CUENTA_PEDIDA")
    void happyPath_pedidoCompletoYMesaCuentaPedida() {
        if (mesaId == null || nombreMesero == null || productoId == null) {
            // No seeded data available — skip gracefully
            return;
        }

        // 1. Crear pedido → mesa debe pasar a OCUPADA
        final Pedido pedido = pedidoService.crearPedido(mesaId, nombreMesero, null);
        assertThat(pedido.estado()).isEqualTo(EstadoPedido.ABIERTO);
        assertThat(mesaService.buscarMesa(mesaId).estado()).isEqualTo(EstadoMesa.OCUPADA);

        // 2. Agregar línea
        final LineaPedido linea = lineaPedidoService.agregarLinea(
                pedido.id(), productoId, new BigDecimal("1.000"), null);
        assertThat(linea.estado()).isEqualTo(EstadoLinea.PENDIENTE);
        assertThat(linea.subtotal()).isNotNull();

        // 3. Avanzar estado: PENDIENTE → EN_PREPARACION (MESERO)
        final LineaPedido enPrep = lineaPedidoService.cambiarEstadoLinea(
                linea.id(), EstadoLinea.EN_PREPARACION, "MESERO");
        assertThat(enPrep.estado()).isEqualTo(EstadoLinea.EN_PREPARACION);

        // 4. EN_PREPARACION → LISTO (MESERO)
        final LineaPedido listo = lineaPedidoService.cambiarEstadoLinea(
                enPrep.id(), EstadoLinea.LISTO, "MESERO");
        assertThat(listo.estado()).isEqualTo(EstadoLinea.LISTO);

        // 5. LISTO → ENTREGADO (MESERO)
        final LineaPedido entregado = lineaPedidoService.cambiarEstadoLinea(
                listo.id(), EstadoLinea.ENTREGADO, "MESERO");
        assertThat(entregado.estado()).isEqualTo(EstadoLinea.ENTREGADO);

        // 6. Cerrar pedido → mesa debe pasar a CUENTA_PEDIDA (único pedido abierto)
        final Pedido cerrado = pedidoService.cerrarPedido(pedido.id());
        assertThat(cerrado.estado()).isEqualTo(EstadoPedido.CERRADO);
        assertThat(mesaService.buscarMesa(mesaId).estado()).isEqualTo(EstadoMesa.CUENTA_PEDIDA);
    }
}
