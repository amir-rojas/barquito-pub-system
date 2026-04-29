package com.barquito.caja;

import com.barquito.autenticacion.infrastructure.UsuarioEntity;
import com.barquito.autenticacion.infrastructure.UsuarioJpaRepository;
import com.barquito.caja.application.VentaConDetalles;
import com.barquito.caja.application.VentaService;
import com.barquito.caja.domain.EstadoVenta;
import com.barquito.caja.domain.MetodoPago;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.application.ZonaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.Mesa;
import com.barquito.pedidos.application.LineaPedidoService;
import com.barquito.pedidos.application.PedidoService;
import com.barquito.pedidos.domain.EstadoLinea;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.productos.domain.CategoriaProducto;
import com.barquito.productos.domain.Producto;
import com.barquito.productos.infrastructure.ProductoEntity;
import com.barquito.productos.infrastructure.ProductoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for the {@code caja} bounded context.
 *
 * <p>Requires Docker. Skipped gracefully if Docker is unavailable.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class VentaIntegrationIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private VentaService ventaService;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private LineaPedidoService lineaPedidoService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ZonaService zonaService;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private ProductoJpaRepository productoJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long zonaId;
    private String cajeroUsername;

    @BeforeEach
    void setUp() {
        zonaId = zonaService.crearZona(
                "TestZona-" + System.nanoTime(), "Zona para tests de caja", 10).id();
        cajeroUsername = "cajero-" + System.nanoTime();
        usuarioJpaRepository.save(new UsuarioEntity(
                null, cajeroUsername,
                passwordEncoder.encode("pass"),
                "mesero", true));
    }

    // -----------------------------------------------------------------------
    // Helper: creates a CERRADO pedido with ENTREGADO lineas on a CUENTA_PEDIDA mesa
    // -----------------------------------------------------------------------

    private record PedidoConMesa(Long pedidoId, Long mesaId, Long productoId) {}

    private PedidoConMesa crearPedidoCerrado(final String meseroUsername) {
        final Mesa mesa = mesaService.crearMesa("M-" + System.nanoTime(), zonaId, null);
        final Long prodId = insertProducto("Birra-" + System.nanoTime(), BigDecimal.valueOf(5.00));

        // Create and configure pedido
        final Pedido pedido = pedidoService.crearPedido(mesa.id(), meseroUsername, null);
        final var linea = lineaPedidoService.agregarLinea(
                pedido.id(), prodId, BigDecimal.valueOf(2), null);
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.EN_PREPARACION, "ADMIN");
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.LISTO, "ADMIN");
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.ENTREGADO, "ADMIN");
        pedidoService.cerrarPedido(pedido.id());

        return new PedidoConMesa(pedido.id(), mesa.id(), prodId);
    }

    private Long insertProducto(final String nombre, final BigDecimal precio) {
        final Producto producto = new Producto(null, nombre, precio, null, CategoriaProducto.OTRO, true, true, null);
        return productoJpaRepository.save(ProductoEntity.toEntity(producto)).getId();
    }

    private String crearMeseroUsername() {
        final String nombre = "mesero-" + System.nanoTime();
        usuarioJpaRepository.save(new UsuarioEntity(
                null, nombre, passwordEncoder.encode("pass"), "mesero", true));
        return nombre;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("crearVenta persiste con snapshot de detalles y calcula total")
    void crearVenta_persiste_con_snapshot() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);

        final VentaConDetalles cd = ventaService.crearVenta(p.pedidoId(), cajeroUsername);

        assertThat(cd.venta().id()).isNotNull();
        assertThat(cd.venta().estado()).isEqualTo(EstadoVenta.PENDIENTE);
        assertThat(cd.venta().pedidoId()).isEqualTo(p.pedidoId());
        assertThat(cd.venta().mesaId()).isEqualTo(p.mesaId());
        assertThat(cd.detalles()).isNotEmpty();
        // total debe ser sum de subtotales de detalles
        final BigDecimal expectedTotal = cd.detalles().stream()
                .map(d -> d.subtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(cd.venta().total()).isEqualByComparingTo(expectedTotal);
        // subtotal de cada detalle debe estar poblado por BD
        cd.detalles().forEach(d -> assertThat(d.subtotal()).isNotNull().isPositive());
    }

    @Test
    @DisplayName("crearVenta desde pedido ABIERTO → 409")
    void crearVenta_pedido_abierto_409() {
        final String meseroUsername = crearMeseroUsername();
        final Mesa mesa = mesaService.crearMesa("M-" + System.nanoTime(), zonaId, null);
        final Pedido pedido = pedidoService.crearPedido(mesa.id(), meseroUsername, null);
        // pedido queda ABIERTO

        assertThatThrownBy(() -> ventaService.crearVenta(pedido.id(), cajeroUsername))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("crearVenta doble para mismo pedido → 409")
    void crearVenta_doble_pedido_409() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);

        ventaService.crearVenta(p.pedidoId(), cajeroUsername);

        assertThatThrownBy(() -> ventaService.crearVenta(p.pedidoId(), cajeroUsername))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("crearVenta con todas las líneas CANCELADO → 409")
    void crearVenta_solo_canceladas_409() {
        final String meseroUsername = crearMeseroUsername();
        final Long prodId = insertProducto("Agua-" + System.nanoTime(), BigDecimal.valueOf(2.00));
        final Mesa mesa = mesaService.crearMesa("M-" + System.nanoTime(), zonaId, null);
        final Pedido pedido = pedidoService.crearPedido(mesa.id(), meseroUsername, null);
        final var linea = lineaPedidoService.agregarLinea(pedido.id(), prodId, BigDecimal.ONE, null);
        lineaPedidoService.cancelarLinea(pedido.id(), linea.id(), "MESERO");
        // cerrar pedido con todas las lineas canceladas
        pedidoService.cerrarPedido(pedido.id());

        assertThatThrownBy(() -> ventaService.crearVenta(pedido.id(), cajeroUsername))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("cobrarVenta libera la mesa atómicamente")
    void cobrarVenta_libera_mesa() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);
        final VentaConDetalles created = ventaService.crearVenta(p.pedidoId(), cajeroUsername);

        final VentaConDetalles cobrada = ventaService.cobrarVenta(
                created.venta().id(), MetodoPago.EFECTIVO);

        assertThat(cobrada.venta().estado()).isEqualTo(EstadoVenta.PAGADA);
        assertThat(cobrada.venta().metodoPago()).isEqualTo(MetodoPago.EFECTIVO);
        assertThat(cobrada.venta().pagadoEn()).isNotNull();
        // mesa debe estar DISPONIBLE
        final Mesa mesa = mesaService.buscarMesa(p.mesaId());
        assertThat(mesa.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
    }

    @Test
    @DisplayName("cobrarVenta PAGADA → 409")
    void cobrarVenta_pagada_409() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);
        final VentaConDetalles created = ventaService.crearVenta(p.pedidoId(), cajeroUsername);
        ventaService.cobrarVenta(created.venta().id(), MetodoPago.EFECTIVO);

        assertThatThrownBy(() ->
                ventaService.cobrarVenta(created.venta().id(), MetodoPago.QR))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("cobrarVenta con mesa no en CUENTA_PEDIDA hace rollback")
    void cobrarVenta_mesa_no_cuenta_pedida_rollback() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);
        final VentaConDetalles created = ventaService.crearVenta(p.pedidoId(), cajeroUsername);

        // Cambiar la mesa manualmente a OCUPADA para provocar fallo en liberarMesa
        mesaService.cambiarEstado(p.mesaId(), EstadoMesa.OCUPADA);

        assertThatThrownBy(() ->
                ventaService.cobrarVenta(created.venta().id(), MetodoPago.EFECTIVO))
                .isInstanceOf(VentaOperacionInvalidaException.class);

        // La venta debe haber quedado PENDIENTE (rollback)
        final VentaConDetalles recuperada = ventaService.buscarVenta(created.venta().id());
        assertThat(recuperada.venta().estado()).isEqualTo(EstadoVenta.PENDIENTE);
    }

    @Test
    @DisplayName("anularVenta no libera la mesa")
    void anularVenta_no_libera_mesa() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);
        final VentaConDetalles created = ventaService.crearVenta(p.pedidoId(), cajeroUsername);

        final VentaConDetalles anulada = ventaService.anularVenta(created.venta().id());

        assertThat(anulada.venta().estado()).isEqualTo(EstadoVenta.ANULADA);
        assertThat(anulada.venta().anuladoEn()).isNotNull();
        // mesa permanece en CUENTA_PEDIDA
        final Mesa mesa = mesaService.buscarMesa(p.mesaId());
        assertThat(mesa.estado()).isEqualTo(EstadoMesa.CUENTA_PEDIDA);
    }

    @Test
    @DisplayName("anularVenta PAGADA → 409")
    void anularVenta_pagada_409() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);
        final VentaConDetalles created = ventaService.crearVenta(p.pedidoId(), cajeroUsername);
        ventaService.cobrarVenta(created.venta().id(), MetodoPago.EFECTIVO);

        assertThatThrownBy(() -> ventaService.anularVenta(created.venta().id()))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("productoNombre snapshot es inmutable — renombrar producto no afecta detalles")
    void productoNombre_snapshot_inmutable() {
        final String meseroUsername = crearMeseroUsername();
        final String nombreOriginal = "Producto-" + System.nanoTime();
        final Long prodId = insertProducto(nombreOriginal, BigDecimal.valueOf(10.00));

        final Mesa mesa = mesaService.crearMesa("M-" + System.nanoTime(), zonaId, null);
        final Pedido pedido = pedidoService.crearPedido(mesa.id(), meseroUsername, null);
        final var linea = lineaPedidoService.agregarLinea(pedido.id(), prodId, BigDecimal.ONE, null);
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.EN_PREPARACION, "ADMIN");
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.LISTO, "ADMIN");
        lineaPedidoService.cambiarEstadoLinea(linea.id(), EstadoLinea.ENTREGADO, "ADMIN");
        pedidoService.cerrarPedido(pedido.id());

        final VentaConDetalles created = ventaService.crearVenta(pedido.id(), cajeroUsername);

        // Rename the product via reflection
        renombrarProducto(prodId, "NuevoNombre-" + System.nanoTime());

        // Re-load the venta — detalles must still have the original name
        final VentaConDetalles reloaded = ventaService.buscarVenta(created.venta().id());
        assertThat(reloaded.detalles()).allMatch(d -> d.productoNombre().equals(nombreOriginal));
    }

    @Test
    @DisplayName("total es igual a la suma de los subtotales de los detalles")
    void total_igual_suma_detalles() {
        final String meseroUsername = crearMeseroUsername();
        final PedidoConMesa p = crearPedidoCerrado(meseroUsername);

        final VentaConDetalles cd = ventaService.crearVenta(p.pedidoId(), cajeroUsername);

        final BigDecimal sumDetalles = cd.detalles().stream()
                .map(d -> d.subtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(cd.venta().total()).isEqualByComparingTo(sumDetalles);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void renombrarProducto(final Long productoId, final String nuevoNombre) {
        productoJpaRepository.findById(productoId).ifPresent(e -> {
            final Producto existing = e.toDomain();
            productoJpaRepository.save(ProductoEntity.toEntity(
                existing.actualizar(nuevoNombre, existing.precio(), existing.descripcion(), existing.categoria(), existing.disponible())));
        });
    }
}
