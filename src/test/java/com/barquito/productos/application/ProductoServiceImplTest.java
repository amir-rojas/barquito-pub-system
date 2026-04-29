package com.barquito.productos.application;

import com.barquito.productos.domain.CategoriaProducto;
import com.barquito.productos.domain.Producto;
import com.barquito.productos.domain.ProductoNombreDuplicadoException;
import com.barquito.productos.domain.ProductoNotFoundException;
import com.barquito.productos.domain.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductoServiceImpl}.
 *
 * <p>These are RED tests (Phase 0). No Spring context — pure Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    private Producto productoActivo;
    private Producto productoActivoYDisponible;
    private Producto productoActivoNoDisponible;

    @BeforeEach
    void setUp() {
        productoActivo = new Producto(
                1L, "Cerveza Quilmes", BigDecimal.valueOf(350), "Cerveza nacional",
                CategoriaProducto.CERVEZA, true, true, OffsetDateTime.now()
        );
        productoActivoYDisponible = new Producto(
                2L, "Fernet", BigDecimal.valueOf(500), null,
                CategoriaProducto.ESPIRITUOSO, true, true, OffsetDateTime.now()
        );
        productoActivoNoDisponible = new Producto(
                3L, "Agua Mineral", BigDecimal.valueOf(100), null,
                CategoriaProducto.GASEOSA, false, true, OffsetDateTime.now()
        );
    }

    @Nested
    @DisplayName("crearProducto")
    class CrearProductoTests {

        @Test
        @DisplayName("llama repository.save y retorna ProductoResponse")
        void crearProducto_llama_save_y_retorna_response() {
            final CrearProductoCommand command = new CrearProductoCommand(
                    "Cerveza Quilmes", BigDecimal.valueOf(350), "Cerveza nacional",
                    CategoriaProducto.CERVEZA, true
            );
            when(productoRepository.findByNombreIgnoreCase("Cerveza Quilmes"))
                    .thenReturn(Optional.empty());
            when(productoRepository.save(any())).thenReturn(productoActivo);

            final ProductoResponse response = productoService.crearProducto(command);

            verify(productoRepository).save(any(Producto.class));
            assertThat(response).isNotNull();
            assertThat(response.nombre()).isEqualTo("Cerveza Quilmes");
            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("con nombre duplicado lanza ProductoNombreDuplicadoException")
        void crearProducto_nombre_duplicado_lanza_excepcion() {
            final CrearProductoCommand command = new CrearProductoCommand(
                    "Cerveza Quilmes", BigDecimal.valueOf(350), null,
                    CategoriaProducto.CERVEZA, true
            );
            when(productoRepository.findByNombreIgnoreCase("Cerveza Quilmes"))
                    .thenReturn(Optional.of(productoActivo));

            assertThatThrownBy(() -> productoService.crearProducto(command))
                    .isInstanceOf(ProductoNombreDuplicadoException.class);
        }
    }

    @Nested
    @DisplayName("obtenerProducto")
    class ObtenerProductoTests {

        @Test
        @DisplayName("retorna ProductoResponse cuando existe")
        void obtenerProducto_retorna_response_cuando_existe() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoActivo));

            final ProductoResponse response = productoService.obtenerProducto(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
        }

        @Test
        @DisplayName("lanza ProductoNotFoundException cuando no existe")
        void obtenerProducto_lanza_not_found() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.obtenerProducto(99L))
                    .isInstanceOf(ProductoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listarProductos")
    class ListarProductosTests {

        @Test
        @DisplayName("retorna lista de ProductoResponse con solo activo=true")
        void listarProductos_retorna_solo_activos() {
            when(productoRepository.findAllActivos())
                    .thenReturn(List.of(productoActivo, productoActivoNoDisponible));

            final List<ProductoResponse> responses = productoService.listarProductos();

            assertThat(responses).hasSize(2);
        }
    }

    @Nested
    @DisplayName("listarProductosDisponibles")
    class ListarProductosDisponiblesTests {

        @Test
        @DisplayName("retorna solo los disponibles y activos")
        void listarProductosDisponibles_retorna_disponibles_activos() {
            when(productoRepository.findAllActivosYDisponibles())
                    .thenReturn(List.of(productoActivoYDisponible));

            final List<ProductoResponse> responses = productoService.listarProductosDisponibles();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).disponible()).isTrue();
        }
    }

    @Nested
    @DisplayName("actualizarProducto")
    class ActualizarProductoTests {

        @Test
        @DisplayName("actualiza y retorna ProductoResponse")
        void actualizarProducto_actualiza_y_retorna_response() {
            final ActualizarProductoCommand command = new ActualizarProductoCommand(
                    "Cerveza Quilmes Premium", BigDecimal.valueOf(400), "Edición especial",
                    CategoriaProducto.CERVEZA, true
            );
            final Producto actualizado = new Producto(
                    1L, "Cerveza Quilmes Premium", BigDecimal.valueOf(400), "Edición especial",
                    CategoriaProducto.CERVEZA, true, true, OffsetDateTime.now()
            );
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoActivo));
            when(productoRepository.save(any())).thenReturn(actualizado);

            final ProductoResponse response = productoService.actualizarProducto(1L, command);

            assertThat(response.nombre()).isEqualTo("Cerveza Quilmes Premium");
            assertThat(response.precio()).isEqualByComparingTo(BigDecimal.valueOf(400));
        }

        @Test
        @DisplayName("lanza ProductoNotFoundException cuando no existe")
        void actualizarProducto_lanza_not_found() {
            final ActualizarProductoCommand command = new ActualizarProductoCommand(
                    "Cualquier Nombre", BigDecimal.valueOf(100), null,
                    CategoriaProducto.OTRO, true
            );
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.actualizarProducto(99L, command))
                    .isInstanceOf(ProductoNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("desactivarProducto")
    class DesactivarProductoTests {

        @Test
        @DisplayName("llama repository.save con activo=false (soft delete)")
        void desactivarProducto_guarda_con_activo_false() {
            when(productoRepository.findById(1L)).thenReturn(Optional.of(productoActivo));
            final Producto desactivado = productoActivo.desactivar();
            when(productoRepository.save(any())).thenReturn(desactivado);

            productoService.desactivarProducto(1L);

            verify(productoRepository).save(any(Producto.class));
        }

        @Test
        @DisplayName("lanza ProductoNotFoundException cuando no existe")
        void desactivarProducto_lanza_not_found() {
            when(productoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productoService.desactivarProducto(99L))
                    .isInstanceOf(ProductoNotFoundException.class);
        }
    }
}
