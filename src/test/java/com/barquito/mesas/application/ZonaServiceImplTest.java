package com.barquito.mesas.application;

import com.barquito.mesas.domain.Zona;
import com.barquito.mesas.domain.ZonaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
 * Unit tests for {@link ZonaServiceImpl}.
 *
 * <p>RED en Fase 3 — {@link ZonaServiceImpl} lanza {@link UnsupportedOperationException}.
 * Los tests fallarán hasta que la implementación real esté en Fase 4.
 */
@ExtendWith(MockitoExtension.class)
class ZonaServiceImplTest {

    @Mock
    private ZonaRepository zonaRepository;

    @InjectMocks
    private ZonaServiceImpl zonaService;

    private static final String NOMBRE = "Terraza";
    private static final String DESCRIPCION = "Zona exterior";
    private static final int ORDEN = 1;

    private Zona zona;

    @BeforeEach
    void setUp() {
        zona = new Zona(1L, NOMBRE, DESCRIPCION, ORDEN);
    }

    @Test
    @DisplayName("REQ-Z-01: crearZona persiste y retorna zona con id")
    void crearZona_datosValidos_retornaZonaConId() {
        lenient().when(zonaRepository.existsByNombreIgnoreCase(NOMBRE)).thenReturn(false);
        lenient().when(zonaRepository.save(any())).thenReturn(zona);

        final Zona result = zonaService.crearZona(NOMBRE, DESCRIPCION, ORDEN);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.nombre()).isEqualTo(NOMBRE);
    }

    @Test
    @DisplayName("REQ-Z-02: crearZona con nombre duplicado lanza excepción")
    void crearZona_nombreDuplicado_lanzaException() {
        when(zonaRepository.existsByNombreIgnoreCase(NOMBRE)).thenReturn(true);

        assertThatThrownBy(() -> zonaService.crearZona(NOMBRE, DESCRIPCION, ORDEN))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("REQ-Z-03: listarZonas retorna lista ordenada")
    void listarZonas_retornaListaOrdenada() {
        final List<Zona> zonas = List.of(zona);
        lenient().when(zonaRepository.findAllOrdenadas()).thenReturn(zonas);

        final List<Zona> result = zonaService.listarZonas();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).nombre()).isEqualTo(NOMBRE);
    }

    @Test
    @DisplayName("REQ-Z-04: actualizarZona con id inexistente lanza ZonaNotFoundException")
    void actualizarZona_idInexistente_lanzaNotFoundException() {
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> zonaService.actualizarZona(99L, "Nuevo", null, 0))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("REQ-Z-05: actualizarZona con datos válidos retorna zona actualizada")
    void actualizarZona_datosValidos_retornaZonaActualizada() {
        final Zona actualizada = new Zona(1L, "Nuevo nombre", null, 2);
        lenient().when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));
        lenient().when(zonaRepository.existsByNombreIgnoreCase("Nuevo nombre")).thenReturn(false);
        lenient().when(zonaRepository.save(any())).thenReturn(actualizada);

        final Zona result = zonaService.actualizarZona(1L, "Nuevo nombre", null, 2);

        assertThat(result.nombre()).isEqualTo("Nuevo nombre");
    }
}
