package com.barquito.mesas.application;

import com.barquito.mesas.domain.EstadoMesa;
import com.barquito.mesas.domain.FormaMesa;
import com.barquito.mesas.domain.Mesa;
import com.barquito.mesas.domain.MesaNotFoundException;
import com.barquito.mesas.domain.MesaOperacionInvalidaException;
import com.barquito.mesas.domain.MesaRepository;
import com.barquito.mesas.domain.Zona;
import com.barquito.mesas.domain.ZonaNotFoundException;
import com.barquito.mesas.domain.ZonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MesaServiceImpl}.
 *
 * <p>RED en Fase 3 — {@link MesaServiceImpl} lanza {@link UnsupportedOperationException}.
 * Los tests fallarán hasta que la implementación real esté en Fase 4.
 */
@ExtendWith(MockitoExtension.class)
class MesaServiceImplTest {

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private ZonaRepository zonaRepository;

    @InjectMocks
    private MesaServiceImpl mesaService;

    private Mesa mesaDisponible;
    private Mesa mesaOcupada;
    private Mesa mesaFusionada;
    private Zona zona;

    @BeforeEach
    void setUp() {
        zona = new Zona(1L, "Salón", null, 0);
        mesaDisponible = new Mesa(1L, "1", EstadoMesa.DISPONIBLE, true, 1L, FormaMesa.CIRCULAR, null);
        mesaOcupada = new Mesa(2L, "2", EstadoMesa.OCUPADA, true, 1L, FormaMesa.RECTANGULAR, null);
        mesaFusionada = new Mesa(3L, "3", EstadoMesa.FUSIONADA, true, 1L, null, 1L);
    }

    @Nested
    @DisplayName("crearMesa()")
    class CrearMesa {

        @Test
        @DisplayName("REQ-M-01: crea mesa con zona válida")
        void crearMesa_zonaValida_retornaMesaConId() {
            lenient().when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
            lenient().when(mesaRepository.save(any())).thenReturn(mesaDisponible);

            final Mesa result = mesaService.crearMesa("1", 1L, FormaMesa.CIRCULAR);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
        }

        @Test
        @DisplayName("REQ-M-02: zona inexistente lanza ZonaNotFoundException")
        void crearMesa_zonaInexistente_lanzaException() {
            lenient().when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> mesaService.crearMesa("1", 99L, null))
                    .isInstanceOf(ZonaNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("buscarMesa()")
    class BuscarMesa {

        @Test
        @DisplayName("REQ-M-08: buscar mesa existente retorna la mesa")
        void buscarMesa_existente_retornaMesa() {
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));

            final Mesa result = mesaService.buscarMesa(1L);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
        }

        @Test
        @DisplayName("REQ-M-09: buscar mesa inexistente lanza MesaNotFoundException")
        void buscarMesa_inexistente_lanzaNotFoundException() {
            lenient().when(mesaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> mesaService.buscarMesa(99L))
                    .isInstanceOf(MesaNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listarMesasActivas()")
    class ListarMesasActivas {

        @Test
        @DisplayName("REQ-M-03: retorna lista de mesas activas incluyendo FUSIONADA")
        void listarMesasActivas_retornaTodasIncluyendoFusionadas() {
            lenient().when(mesaRepository.findAllActivas()).thenReturn(List.of(mesaDisponible, mesaFusionada));

            final List<Mesa> result = mesaService.listarMesasActivas();

            assertThat(result).hasSize(2);
            assertThat(result).anyMatch(m -> m.estado() == EstadoMesa.FUSIONADA);
        }
    }

    @Nested
    @DisplayName("cambiarEstado()")
    class CambiarEstado {

        @Test
        @DisplayName("REQ-E-01: DISPONIBLE → OCUPADA es válido")
        void cambiarEstado_disponibleAOcupada_retornaMesaActualizada() {
            final Mesa ocupada = mesaDisponible.conEstado(EstadoMesa.OCUPADA);
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));
            lenient().when(mesaRepository.save(any())).thenReturn(ocupada);

            final Mesa result = mesaService.cambiarEstado(1L, EstadoMesa.OCUPADA);

            assertThat(result.estado()).isEqualTo(EstadoMesa.OCUPADA);
        }

        @Test
        @DisplayName("REQ-E-02: cambiar estado a FUSIONADA directamente lanza MesaOperacionInvalidaException")
        void cambiarEstado_aFusionada_lanzaException() {
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));

            assertThatThrownBy(() -> mesaService.cambiarEstado(1L, EstadoMesa.FUSIONADA))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("REQ-E-03: mesa no encontrada lanza MesaNotFoundException")
        void cambiarEstado_mesaInexistente_lanzaNotFoundException() {
            lenient().when(mesaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> mesaService.cambiarEstado(99L, EstadoMesa.OCUPADA))
                    .isInstanceOf(MesaNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cambiarActiva()")
    class CambiarActiva {

        @Test
        @DisplayName("REQ-M-04: desactivar mesa DISPONIBLE sin secundarias → ok")
        void cambiarActiva_false_mesaDisponibleSinSecundarias_ok() {
            final Mesa inactiva = mesaDisponible.conActiva(false);
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));
            lenient().when(mesaRepository.findSecundariasByMesaPrincipalId(1L)).thenReturn(List.of());
            lenient().when(mesaRepository.save(any())).thenReturn(inactiva);

            final Mesa result = mesaService.cambiarActiva(1L, false);

            assertThat(result.activa()).isFalse();
        }

        @Test
        @DisplayName("REQ-M-05: desactivar mesa FUSIONADA lanza MesaOperacionInvalidaException")
        void cambiarActiva_false_mesaFusionada_lanzaException() {
            lenient().when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesaFusionada));

            assertThatThrownBy(() -> mesaService.cambiarActiva(3L, false))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("REQ-M-06: desactivar mesa OCUPADA lanza MesaOperacionInvalidaException")
        void cambiarActiva_false_mesaOcupada_lanzaException() {
            lenient().when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesaOcupada));

            assertThatThrownBy(() -> mesaService.cambiarActiva(2L, false))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("REQ-M-07: desactivar principal con secundarias activas lanza MesaOperacionInvalidaException")
        void cambiarActiva_false_principalConSecundarias_lanzaException() {
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));
            lenient().when(mesaRepository.findSecundariasByMesaPrincipalId(1L)).thenReturn(List.of(mesaFusionada));

            assertThatThrownBy(() -> mesaService.cambiarActiva(1L, false))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("fusionarMesa()")
    class FusionarMesa {

        @Test
        @DisplayName("REQ-F-01: fusión válida retorna secundaria en estado FUSIONADA")
        void fusionarMesa_valida_retornaSecundariaFusionada() {
            final Mesa secundaria = new Mesa(4L, "4", EstadoMesa.DISPONIBLE, true, 1L, null, null);
            final Mesa fusionada = new Mesa(4L, "4", EstadoMesa.FUSIONADA, true, 1L, null, 1L);
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));
            lenient().when(mesaRepository.findById(4L)).thenReturn(Optional.of(secundaria));
            lenient().when(mesaRepository.save(any())).thenReturn(fusionada);

            final Mesa result = mesaService.fusionarMesa(1L, 4L);

            assertThat(result.estado()).isEqualTo(EstadoMesa.FUSIONADA);
            assertThat(result.mesaPrincipalId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("REQ-F-02: fusionar mesa consigo misma lanza MesaOperacionInvalidaException")
        void fusionarMesa_consigaMisma_lanzaException() {
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));

            assertThatThrownBy(() -> mesaService.fusionarMesa(1L, 1L))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("REQ-F-03: principal en estado FUSIONADA no puede ser principal")
        void fusionarMesa_principalFusionada_lanzaException() {
            lenient().when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesaFusionada));
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));

            assertThatThrownBy(() -> mesaService.fusionarMesa(3L, 1L))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("REQ-F-04: secundaria OCUPADA no puede fusionarse")
        void fusionarMesa_secundariaOcupada_lanzaException() {
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));
            lenient().when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesaOcupada));

            assertThatThrownBy(() -> mesaService.fusionarMesa(1L, 2L))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }

        @Test
        @DisplayName("REQ-F-07: anti-ciclo — A es secundaria de B, intentar fusionar B como secundaria de A lanza MesaOperacionInvalidaException")
        void fusionarMesa_anticiclo_lanzaException() {
            // Scenario: A (id=4) is already secondary of B (id=1), so A.mesaPrincipalId = 1, A.estado = FUSIONADA.
            // Attempting fusionarMesa(principalId=1, secundariaId=4) tries to fuse A again under B.
            // A is already FUSIONADA → puedeSerFusionadaComoSecundaria() returns false → MesaOperacionInvalidaException.
            // This guards against re-fusing a mesa that is already part of a fusion (cycle prevention).
            final Mesa mesaAFusionada = new Mesa(4L, "4", EstadoMesa.FUSIONADA, true, 1L, null, 1L);

            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));
            lenient().when(mesaRepository.findById(4L)).thenReturn(Optional.of(mesaAFusionada));

            assertThatThrownBy(() -> mesaService.fusionarMesa(1L, 4L))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }
    }

    @Nested
    @DisplayName("desfusionarMesa()")
    class DesfusionarMesa {

        @Test
        @DisplayName("REQ-F-05: desfusionar retorna secundaria en DISPONIBLE con mesaPrincipalId=null")
        void desfusionarMesa_valida_retornaDisponible() {
            final Mesa disponible = new Mesa(3L, "3", EstadoMesa.DISPONIBLE, true, 1L, null, null);
            lenient().when(mesaRepository.findById(3L)).thenReturn(Optional.of(mesaFusionada));
            lenient().when(mesaRepository.save(any())).thenReturn(disponible);

            final Mesa result = mesaService.desfusionarMesa(3L);

            assertThat(result.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
            assertThat(result.mesaPrincipalId()).isNull();
        }

        @Test
        @DisplayName("REQ-F-06: desfusionar mesa no fusionada lanza MesaOperacionInvalidaException")
        void desfusionarMesa_noFusionada_lanzaException() {
            lenient().when(mesaRepository.findById(1L)).thenReturn(Optional.of(mesaDisponible));

            assertThatThrownBy(() -> mesaService.desfusionarMesa(1L))
                    .isInstanceOf(MesaOperacionInvalidaException.class);
        }
    }
}
