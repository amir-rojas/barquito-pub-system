package com.barquito.mesas.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link Mesa} domain record — invariants and copy-with helpers.
 */
class MesaDomainTest {

    private static Mesa mesaDisponible() {
        return new Mesa(1L, "1", EstadoMesa.DISPONIBLE, true, 1L, FormaMesa.CIRCULAR, null);
    }

    private static Mesa mesaOcupada() {
        return new Mesa(2L, "2", EstadoMesa.OCUPADA, true, 1L, FormaMesa.RECTANGULAR, null);
    }

    private static Mesa mesaFusionada() {
        return new Mesa(3L, "3", EstadoMesa.FUSIONADA, true, 1L, null, 1L);
    }

    private static Mesa mesaCuentaPedida() {
        return new Mesa(4L, "4", EstadoMesa.CUENTA_PEDIDA, true, 1L, null, null);
    }

    @Nested
    @DisplayName("esFusionada()")
    class EsFusionada {

        @Test
        @DisplayName("retorna true cuando mesaPrincipalId tiene valor")
        void esFusionada_conPrincipalId_retornaTrue() {
            assertThat(mesaFusionada().esFusionada()).isTrue();
        }

        @Test
        @DisplayName("retorna false cuando mesaPrincipalId es null")
        void esFusionada_sinPrincipalId_retornaFalse() {
            assertThat(mesaDisponible().esFusionada()).isFalse();
        }
    }

    @Nested
    @DisplayName("puedeSerPrincipal()")
    class PuedeSerPrincipal {

        @Test
        @DisplayName("DISPONIBLE y no fusionada → puede ser principal")
        void disponibleNoFusionada_puedeSerPrincipal() {
            assertThat(mesaDisponible().puedeSerPrincipal()).isTrue();
        }

        @Test
        @DisplayName("OCUPADA y no fusionada → puede ser principal")
        void ocupadaNoFusionada_puedeSerPrincipal() {
            assertThat(mesaOcupada().puedeSerPrincipal()).isTrue();
        }

        @Test
        @DisplayName("FUSIONADA → no puede ser principal")
        void fusionada_noPuedeSerPrincipal() {
            assertThat(mesaFusionada().puedeSerPrincipal()).isFalse();
        }

        @Test
        @DisplayName("CUENTA_PEDIDA → no puede ser principal")
        void cuentaPedida_noPuedeSerPrincipal() {
            assertThat(mesaCuentaPedida().puedeSerPrincipal()).isFalse();
        }

        @Test
        @DisplayName("mesa con mesaPrincipalId no null → no puede ser principal aunque esté DISPONIBLE")
        void disponibleConPrincipalId_noPuedeSerPrincipal() {
            final Mesa mesa = new Mesa(5L, "5", EstadoMesa.DISPONIBLE, true, 1L, null, 99L);
            assertThat(mesa.puedeSerPrincipal()).isFalse();
        }
    }

    @Nested
    @DisplayName("puedeSerFusionadaComoSecundaria()")
    class PuedeSerFusionadaComoSecundaria {

        @Test
        @DisplayName("DISPONIBLE sin mesaPrincipalId → puede ser secundaria")
        void disponibleSinPrincipal_puedeSerSecundaria() {
            assertThat(mesaDisponible().puedeSerFusionadaComoSecundaria()).isTrue();
        }

        @Test
        @DisplayName("OCUPADA → no puede ser secundaria")
        void ocupada_noPuedeSerSecundaria() {
            assertThat(mesaOcupada().puedeSerFusionadaComoSecundaria()).isFalse();
        }

        @Test
        @DisplayName("FUSIONADA → no puede ser secundaria (ya lo es)")
        void fusionada_noPuedeSerSecundaria() {
            assertThat(mesaFusionada().puedeSerFusionadaComoSecundaria()).isFalse();
        }

        @Test
        @DisplayName("DISPONIBLE con mesaPrincipalId → no puede ser secundaria otra vez")
        void disponibleConPrincipalId_noPuedeSerSecundaria() {
            final Mesa mesa = new Mesa(5L, "5", EstadoMesa.DISPONIBLE, true, 1L, null, 99L);
            assertThat(mesa.puedeSerFusionadaComoSecundaria()).isFalse();
        }
    }

    @Nested
    @DisplayName("conEstado()")
    class ConEstado {

        @Test
        @DisplayName("retorna nueva instancia con el estado actualizado")
        void conEstado_retornaNuevaInstanciaConEstadoActualizado() {
            final Mesa original = mesaDisponible();
            final Mesa actualizada = original.conEstado(EstadoMesa.OCUPADA);

            assertThat(actualizada.estado()).isEqualTo(EstadoMesa.OCUPADA);
            assertThat(actualizada.id()).isEqualTo(original.id());
            assertThat(actualizada.numero()).isEqualTo(original.numero());
            assertThat(actualizada).isNotSameAs(original);
        }

        @Test
        @DisplayName("no muta la instancia original")
        void conEstado_noMutaOriginal() {
            final Mesa original = mesaDisponible();
            original.conEstado(EstadoMesa.OCUPADA);

            assertThat(original.estado()).isEqualTo(EstadoMesa.DISPONIBLE);
        }
    }

    @Nested
    @DisplayName("conActiva()")
    class ConActiva {

        @Test
        @DisplayName("retorna nueva instancia con activa=false")
        void conActiva_false_retornaNuevaInstancia() {
            final Mesa original = mesaDisponible();
            final Mesa inactiva = original.conActiva(false);

            assertThat(inactiva.activa()).isFalse();
            assertThat(inactiva.id()).isEqualTo(original.id());
            assertThat(original.activa()).isTrue();
        }
    }

    @Nested
    @DisplayName("conAtributosFisicos()")
    class ConAtributosFisicos {

        @Test
        @DisplayName("actualiza numero cuando se provee valor no null")
        void conAtributosFisicos_actualizaNumero() {
            final Mesa original = mesaDisponible();
            final Mesa actualizada = original.conAtributosFisicos("A1", null, null);

            assertThat(actualizada.numero()).isEqualTo("A1");
            assertThat(actualizada.forma()).isEqualTo(original.forma());
            assertThat(actualizada.zonaId()).isEqualTo(original.zonaId());
        }

        @Test
        @DisplayName("mantiene valores originales cuando los parámetros son null")
        void conAtributosFisicos_nullMantienValoresOriginales() {
            final Mesa original = mesaDisponible();
            final Mesa actualizada = original.conAtributosFisicos(null, null, null);

            assertThat(actualizada.numero()).isEqualTo(original.numero());
            assertThat(actualizada.forma()).isEqualTo(original.forma());
            assertThat(actualizada.zonaId()).isEqualTo(original.zonaId());
        }

        @Test
        @DisplayName("actualiza forma y zonaId cuando se proveen")
        void conAtributosFisicos_actualizaFormaYZona() {
            final Mesa original = mesaDisponible();
            final Mesa actualizada = original.conAtributosFisicos(null, FormaMesa.RECTANGULAR, 2L);

            assertThat(actualizada.forma()).isEqualTo(FormaMesa.RECTANGULAR);
            assertThat(actualizada.zonaId()).isEqualTo(2L);
        }
    }
}
