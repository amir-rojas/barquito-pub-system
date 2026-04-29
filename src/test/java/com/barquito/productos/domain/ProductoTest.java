package com.barquito.productos.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the {@link Producto} domain record.
 *
 * <p>These are RED tests (Phase 0). They will fail until the production code
 * is implemented in Phase 1.
 */
class ProductoTest {

    private static final OffsetDateTime NOW = OffsetDateTime.now();

    private Producto productoCompleto() {
        return new Producto(
                1L,
                "Cerveza Artesanal",
                BigDecimal.valueOf(350.00),
                "Una cerveza artesanal de malta",
                CategoriaProducto.CERVEZA,
                true,
                true,
                NOW
        );
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("crea record con todos los campos correctamente")
        void constructor_crea_con_todos_los_campos() {
            final Producto producto = productoCompleto();

            assertThat(producto.id()).isEqualTo(1L);
            assertThat(producto.nombre()).isEqualTo("Cerveza Artesanal");
            assertThat(producto.precio()).isEqualByComparingTo(BigDecimal.valueOf(350.00));
            assertThat(producto.descripcion()).isEqualTo("Una cerveza artesanal de malta");
            assertThat(producto.categoria()).isEqualTo(CategoriaProducto.CERVEZA);
            assertThat(producto.disponible()).isTrue();
            assertThat(producto.activo()).isTrue();
            assertThat(producto.creadoEn()).isEqualTo(NOW);
        }
    }

    @Nested
    @DisplayName("desactivar()")
    class DesactivarTests {

        @Test
        @DisplayName("retorna nueva instancia con activo=false")
        void desactivar_retorna_nuevo_producto_con_activo_false() {
            final Producto original = productoCompleto();

            final Producto desactivado = original.desactivar();

            assertThat(desactivado.activo()).isFalse();
            assertThat(desactivado.id()).isEqualTo(original.id());
            assertThat(desactivado.nombre()).isEqualTo(original.nombre());
            assertThat(desactivado.precio()).isEqualByComparingTo(original.precio());
            assertThat(desactivado.disponible()).isEqualTo(original.disponible());
        }

        @Test
        @DisplayName("no modifica el original")
        void desactivar_no_muta_original() {
            final Producto original = productoCompleto();

            original.desactivar();

            assertThat(original.activo()).isTrue();
        }
    }

    @Nested
    @DisplayName("actualizar()")
    class ActualizarTests {

        @Test
        @DisplayName("retorna nueva instancia con los campos actualizados")
        void actualizar_retorna_nuevo_producto_actualizado() {
            final Producto original = productoCompleto();
            final BigDecimal nuevoPrecio = BigDecimal.valueOf(500.00);

            final Producto actualizado = original.actualizar(
                    "Cerveza Premium",
                    nuevoPrecio,
                    "Descripción actualizada",
                    CategoriaProducto.ESPIRITUOSO,
                    false
            );

            assertThat(actualizado.nombre()).isEqualTo("Cerveza Premium");
            assertThat(actualizado.precio()).isEqualByComparingTo(nuevoPrecio);
            assertThat(actualizado.descripcion()).isEqualTo("Descripción actualizada");
            assertThat(actualizado.categoria()).isEqualTo(CategoriaProducto.ESPIRITUOSO);
            assertThat(actualizado.disponible()).isFalse();
        }

        @Test
        @DisplayName("preserva id, activo y creadoEn al actualizar")
        void actualizar_preserva_campos_no_actualizables() {
            final Producto original = productoCompleto();

            final Producto actualizado = original.actualizar(
                    "Nuevo Nombre",
                    BigDecimal.valueOf(100),
                    "Desc",
                    CategoriaProducto.GASEOSA,
                    true
            );

            assertThat(actualizado.id()).isEqualTo(original.id());
            assertThat(actualizado.activo()).isEqualTo(original.activo());
            assertThat(actualizado.creadoEn()).isEqualTo(original.creadoEn());
        }
    }

    @Nested
    @DisplayName("CategoriaProducto enum")
    class CategoriaProductoTests {

        @Test
        @DisplayName("tiene exactamente 4 valores: CERVEZA, ESPIRITUOSO, GASEOSA, OTRO")
        void enum_tiene_4_valores() {
            final CategoriaProducto[] valores = CategoriaProducto.values();

            assertThat(valores).hasSize(4);
            assertThat(valores).containsExactlyInAnyOrder(
                    CategoriaProducto.CERVEZA,
                    CategoriaProducto.ESPIRITUOSO,
                    CategoriaProducto.GASEOSA,
                    CategoriaProducto.OTRO
            );
        }
    }
}
