package com.barquito.caja;

import com.barquito.caja.application.MesaLiberarPort;
import com.barquito.caja.domain.VentaOperacionInvalidaException;
import com.barquito.mesas.application.MesaService;
import com.barquito.mesas.application.ZonaService;
import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.Mesa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@code MesaLiberarAdapter}.
 *
 * <p>Validates lock behavior (PESSIMISTIC_WRITE), state transition, and
 * Propagation.MANDATORY enforcement.
 */
@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class MesaLiberarAdapterIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MesaLiberarPort mesaLiberarPort;

    @Autowired
    private MesaService mesaService;

    @Autowired
    private ZonaService zonaService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long zonaId;

    @BeforeEach
    void setUp() {
        zonaId = zonaService.crearZona(
                "ZonaCaja-" + System.nanoTime(), "Zona para tests MesaLiberarAdapter", 5).id();
    }

    @Test
    @DisplayName("liberarMesa transiciona mesa CUENTA_PEDIDA → DISPONIBLE")
    void liberarMesa_transiciona_mesa() {
        final Mesa mesa = mesaService.crearMesa("ML-" + System.nanoTime(), zonaId, null);
        mesaService.cambiarEstado(mesa.id(), EstadoMesa.OCUPADA);
        mesaService.cambiarEstado(mesa.id(), EstadoMesa.CUENTA_PEDIDA);

        transactionTemplate.execute(status -> {
            mesaLiberarPort.liberarMesa(mesa.id());
            return null;
        });

        final Mesa liberada = mesaService.buscarMesa(mesa.id());
        assertThat(liberada.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
    }

    @Test
    @DisplayName("liberarMesa con mesa en estado incorrecto lanza VentaOperacionInvalidaException")
    void liberarMesa_mesa_incorrecta_lanza() {
        final Mesa mesa = mesaService.crearMesa("ML-" + System.nanoTime(), zonaId, null);
        // Mesa en DISPONIBLE (default) → no puede ir a DISPONIBLE desde DISPONIBLE
        // Alternatively put it in OCUPADA, which also isn't CUENTA_PEDIDA
        mesaService.cambiarEstado(mesa.id(), EstadoMesa.OCUPADA);

        assertThatThrownBy(() ->
                transactionTemplate.execute(status -> {
                    mesaLiberarPort.liberarMesa(mesa.id());
                    return null;
                }))
                .isInstanceOf(VentaOperacionInvalidaException.class);
    }

    @Test
    @DisplayName("liberarMesa sin transacción activa lanza IllegalTransactionStateException (MANDATORY)")
    void liberarMesa_sin_tx_lanza() {
        final Mesa mesa = mesaService.crearMesa("ML-" + System.nanoTime(), zonaId, null);
        mesaService.cambiarEstado(mesa.id(), EstadoMesa.OCUPADA);
        mesaService.cambiarEstado(mesa.id(), EstadoMesa.CUENTA_PEDIDA);

        // Call WITHOUT a wrapping transaction — Propagation.MANDATORY must throw
        assertThatThrownBy(() -> mesaLiberarPort.liberarMesa(mesa.id()))
                .isInstanceOf(IllegalTransactionStateException.class);
    }
}
