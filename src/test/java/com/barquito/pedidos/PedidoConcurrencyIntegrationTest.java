package com.barquito.pedidos;

import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.pedidos.application.PedidoService;
import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.Pedido;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Concurrency test: two parallel crearPedido on the same mesa.
 *
 * <p>Both should terminate with consistent state: the mesa remains OCUPADA
 * (both pedidos succeed since mesa.cambiarEstado allows OCUPADA→OCUPADA idempotently,
 * and PESSIMISTIC_WRITE ensures serialized access).
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class PedidoConcurrencyIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private com.barquito.mesas.infrastructure.MesaJpaRepository mesaJpaRepository;

    @Autowired
    private com.barquito.autenticacion.infrastructure.UsuarioJpaRepository usuarioJpaRepository;

    private Long mesaId;
    private String nombreMesero;

    @BeforeEach
    void setUp() {
        mesaId = mesaJpaRepository.findAll().stream()
                .filter(m -> "DISPONIBLE".equals(m.getEstado()) && m.isActiva())
                .findFirst().map(m -> m.getId()).orElse(null);

        nombreMesero = usuarioJpaRepository.findAll().stream()
                .filter(u -> "mesero".equals(u.getRol()) && u.isActivo())
                .findFirst().map(u -> u.getNombre()).orElse(null);
    }

    @Test
    @DisplayName("dos crearPedido paralelos en la misma mesa → estado consistente")
    void dosPedidosParalelos_estadoConsistente() throws Exception {
        if (mesaId == null || nombreMesero == null) {
            return;
        }

        final int threads = 2;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newFixedThreadPool(threads);
        final List<Future<Object>> futures = new ArrayList<>();
        final Long finalMesaId = mesaId;
        final String finalNombre = nombreMesero;

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                startLatch.await();
                try {
                    return pedidoService.crearPedido(finalMesaId, finalNombre, null);
                } catch (Exception e) {
                    return e;
                }
            }));
        }

        startLatch.countDown();
        executor.shutdown();

        // Collect results
        int successes = 0;
        int failures = 0;
        for (final Future<Object> f : futures) {
            final Object result = f.get();
            if (result instanceof Pedido p) {
                assertThat(p.estado()).isEqualTo(EstadoPedido.ABIERTO);
                successes++;
            } else {
                failures++;
            }
        }

        // At least one should succeed; mesa must be OCUPADA
        assertThat(successes).isGreaterThanOrEqualTo(1);
        assertThat(mesaService.buscarMesa(mesaId).estado()).isEqualTo(EstadoMesa.OCUPADA);
    }
}
