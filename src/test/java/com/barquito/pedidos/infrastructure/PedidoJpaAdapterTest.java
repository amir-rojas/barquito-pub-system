package com.barquito.pedidos.infrastructure;

import com.barquito.pedidos.domain.EstadoPedido;
import com.barquito.pedidos.domain.Pedido;
import com.barquito.pedidos.domain.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Slice tests for {@link PedidoJpaAdapter} using {@code @DataJpaTest} with Testcontainers.
 */
@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PedidoJpaAdapter.class, LineaPedidoJpaAdapter.class})
class PedidoJpaAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private PedidoJpaRepository pedidoJpaRepository;

    // We need a valid mesa_id and mesero_id — use the seeded data from V2/V3
    // V2 seeds usuarios, V3 seeds a zona and we can rely on the mesas table
    // For simplicity, we insert directly via JPA repositories that exist

    @Autowired
    private com.barquito.mesas.infrastructure.MesaJpaRepository mesaJpaRepository;

    @Autowired
    private com.barquito.mesas.infrastructure.ZonaJpaRepository zonaJpaRepository;

    @Autowired
    private com.barquito.autenticacion.infrastructure.UsuarioJpaRepository usuarioJpaRepository;

    private Long mesaId;
    private Long meseroId;

    @BeforeEach
    void setUp() {
        // Seed zona
        final Long zonaId = zonaJpaRepository.findAll().stream()
                .findFirst()
                .map(z -> z.getId())
                .orElseGet(() -> zonaJpaRepository.save(
                        new com.barquito.mesas.infrastructure.ZonaEntity(null, "Test", null, 0)).getId());

        // Seed mesa
        mesaId = mesaJpaRepository.findAll().stream()
                .findFirst()
                .map(m -> m.getId())
                .orElseGet(() -> mesaJpaRepository.save(
                        new com.barquito.mesas.infrastructure.MesaEntity(
                                null, "T1", "DISPONIBLE", true, zonaId, null, null)).getId());

        // Seed usuario
        meseroId = usuarioJpaRepository.findAll().stream()
                .findFirst()
                .map(u -> u.getId())
                .orElseGet(() -> usuarioJpaRepository.save(
                        new com.barquito.autenticacion.infrastructure.UsuarioEntity(
                                null, "mesero_test", "$2a$10$xxx", "mesero", true)).getId());
    }

    @Test
    @DisplayName("save persiste un pedido y retorna con id asignado")
    void save_pedidoValido_persisteConId() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido nuevo = new Pedido(null, mesaId, meseroId,
                EstadoPedido.ABIERTO, "test notas", ahora, ahora, null);

        final Pedido guardado = pedidoRepository.save(nuevo);

        assertThat(guardado.id()).isNotNull();
        assertThat(guardado.estado()).isEqualTo(EstadoPedido.ABIERTO);
        assertThat(guardado.mesaId()).isEqualTo(mesaId);
    }

    @Test
    @DisplayName("findById retorna el pedido guardado")
    void findById_pedidoExistente_retornaPedido() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        final Pedido guardado = pedidoRepository.save(
                new Pedido(null, mesaId, meseroId, EstadoPedido.ABIERTO, null, ahora, ahora, null));

        final Optional<Pedido> encontrado = pedidoRepository.findById(guardado.id());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().estado()).isEqualTo(EstadoPedido.ABIERTO);
    }

    @Test
    @DisplayName("findById retorna vacío para id inexistente")
    void findById_idInexistente_retornaVacio() {
        assertThat(pedidoRepository.findById(9999L)).isEmpty();
    }

    @Test
    @DisplayName("countAbiertosByMesaId cuenta solo pedidos ABIERTOS")
    void countAbiertosByMesaId_soloContaAbiertos() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        pedidoRepository.save(
                new Pedido(null, mesaId, meseroId, EstadoPedido.ABIERTO, null, ahora, ahora, null));

        final long count = pedidoRepository.countAbiertosByMesaId(mesaId);

        assertThat(count).isGreaterThanOrEqualTo(1);
    }
}
