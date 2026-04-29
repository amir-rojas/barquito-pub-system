package com.barquito;

import com.barquito.autenticacion.infrastructure.UsuarioJpaRepository;
import com.barquito.caja.application.MesaLiberarPort;
import com.barquito.caja.application.PedidoPort;
import com.barquito.caja.application.RegistrarTransaccionPort;
import com.barquito.caja.application.UsuarioLookupPort;
import com.barquito.caja.domain.DetalleVentaRepository;
import com.barquito.caja.domain.VentaRepository;
import com.barquito.caja.infrastructure.DetalleVentaJpaRepository;
import com.barquito.caja.infrastructure.VentaJpaRepository;
import com.barquito.finanzas.infrastructure.TransaccionJpaRepository;
import com.barquito.mesas.infrastructure.MesaJpaRepository;
import com.barquito.reportes.infrastructure.ReporteFinanzasJpaRepository;
import com.barquito.reportes.infrastructure.ReporteVentasJpaRepository;
import com.barquito.mesas.infrastructure.ZonaJpaRepository;
import com.barquito.pedidos.infrastructure.LineaPedidoJpaRepository;
import com.barquito.pedidos.infrastructure.PedidoJpaRepository;
import com.barquito.productos.infrastructure.ProductoJpaRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Smoke test que verifica que el contexto de Spring carga correctamente.
 *
 * <p>Excluye autoconfiguraciones de base de datos para no requerir una BD activa,
 * y mockea el repositorio JPA para satisfacer las dependencias de la capa de infraestructura.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=" +
    "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
    "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration," +
    "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration," +
    "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration",
    "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
    "jwt.expiration-ms=28800000"
})
class BackendApplicationTests {

    @MockitoBean
    private UsuarioJpaRepository usuarioJpaRepository;

    @MockitoBean
    private MesaJpaRepository mesaJpaRepository;

    @MockitoBean
    private ZonaJpaRepository zonaJpaRepository;

    @MockitoBean
    private PedidoJpaRepository pedidoJpaRepository;

    @MockitoBean
    private LineaPedidoJpaRepository lineaPedidoJpaRepository;

    @MockitoBean
    private ProductoJpaRepository productoJpaRepository;

    @MockitoBean
    private EntityManager entityManager;

    @MockitoBean
    private VentaRepository ventaRepository;

    @MockitoBean
    private DetalleVentaRepository detalleVentaRepository;

    @MockitoBean
    private VentaJpaRepository ventaJpaRepository;

    @MockitoBean
    private DetalleVentaJpaRepository detalleVentaJpaRepository;

    @MockitoBean
    private PedidoPort pedidoPort;

    @MockitoBean
    private MesaLiberarPort mesaLiberarPort;

    @MockitoBean
    private UsuarioLookupPort usuarioLookupPort;

    @MockitoBean
    private RegistrarTransaccionPort registrarTransaccionPort;

    @MockitoBean
    private TransaccionJpaRepository transaccionJpaRepository;

    @MockitoBean
    private ReporteVentasJpaRepository reporteVentasJpaRepository;

    @MockitoBean
    private ReporteFinanzasJpaRepository reporteFinanzasJpaRepository;

    @Test
    void contextLoads() {
    }
}
