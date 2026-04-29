package com.barquito.productos.api;

import com.barquito.autenticacion.infrastructure.JwtProperties;
import com.barquito.autenticacion.infrastructure.JwtService;
import com.barquito.productos.application.ProductoResponse;
import com.barquito.productos.application.ProductoService;
import com.barquito.productos.domain.CategoriaProducto;
import com.barquito.productos.domain.ProductoNotFoundException;
import com.barquito.shared.exception.GlobalExceptionHandler;
import com.barquito.shared.infrastructure.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc slice tests for {@link ProductoController}.
 *
 * <p>Tests de seguridad (roles) y lógica con mocks del servicio.
 */
@WebMvcTest(ProductoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductoService productoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtProperties jwtProperties;

    private static final String BASE_URL = "/api/productos";

    private ProductoResponse productoResponse() {
        return new ProductoResponse(
                1L, "Cerveza Quilmes", BigDecimal.valueOf(350), "Descripción",
                CategoriaProducto.CERVEZA, true, true, OffsetDateTime.now()
        );
    }

    private String crearProductoRequestJson() throws Exception {
        return objectMapper.writeValueAsString(new CrearProductoRequest(
                "Cerveza Quilmes",
                BigDecimal.valueOf(350),
                "Descripción",
                CategoriaProducto.CERVEZA,
                true
        ));
    }

    private String actualizarProductoRequestJson() throws Exception {
        return objectMapper.writeValueAsString(new ActualizarProductoRequest(
                "Cerveza Quilmes Premium",
                BigDecimal.valueOf(400),
                "Edición especial",
                CategoriaProducto.CERVEZA,
                true
        ));
    }

    @Nested
    @DisplayName("POST /api/productos")
    class CrearProductoTests {

        @Test
        @DisplayName("ADMIN → 201 Created")
        @WithMockUser(roles = "ADMIN")
        void crear_admin_retorna_201() throws Exception {
            when(productoService.crearProducto(any())).thenReturn(productoResponse());

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(crearProductoRequestJson()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nombre").value("Cerveza Quilmes"));
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void crear_mesero_retorna_403() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(crearProductoRequestJson()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/productos")
    class ListarProductosTests {

        @Test
        @DisplayName("ADMIN → 200 OK")
        @WithMockUser(roles = "ADMIN")
        void listar_admin_retorna_200() throws Exception {
            when(productoService.listarProductos()).thenReturn(List.of(productoResponse()));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nombre").value("Cerveza Quilmes"));
        }

        @Test
        @DisplayName("MESERO → 200 OK")
        @WithMockUser(roles = "MESERO")
        void listar_mesero_retorna_200() throws Exception {
            when(productoService.listarProductos()).thenReturn(List.of(productoResponse()));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/productos/disponibles")
    class ListarDisponiblesTests {

        @Test
        @DisplayName("ADMIN → 200 OK")
        @WithMockUser(roles = "ADMIN")
        void listarDisponibles_admin_retorna_200() throws Exception {
            when(productoService.listarProductosDisponibles()).thenReturn(List.of(productoResponse()));

            mockMvc.perform(get(BASE_URL + "/disponibles"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("MESERO → 200 OK")
        @WithMockUser(roles = "MESERO")
        void listarDisponibles_mesero_retorna_200() throws Exception {
            when(productoService.listarProductosDisponibles()).thenReturn(List.of(productoResponse()));

            mockMvc.perform(get(BASE_URL + "/disponibles"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/productos/{id}")
    class ObtenerProductoTests {

        @Test
        @DisplayName("ADMIN → 200 OK")
        @WithMockUser(roles = "ADMIN")
        void obtener_admin_retorna_200() throws Exception {
            when(productoService.obtenerProducto(1L)).thenReturn(productoResponse());

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("MESERO → 200 OK")
        @WithMockUser(roles = "MESERO")
        void obtener_mesero_retorna_200() throws Exception {
            when(productoService.obtenerProducto(1L)).thenReturn(productoResponse());

            mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("not found → 404 Not Found")
        @WithMockUser(roles = "ADMIN")
        void obtener_not_found_retorna_404() throws Exception {
            when(productoService.obtenerProducto(99L))
                    .thenThrow(new ProductoNotFoundException("Producto 99 no encontrado"));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/productos/{id}")
    class ActualizarProductoTests {

        @Test
        @DisplayName("ADMIN → 200 OK")
        @WithMockUser(roles = "ADMIN")
        void actualizar_admin_retorna_200() throws Exception {
            when(productoService.actualizarProducto(anyLong(), any())).thenReturn(productoResponse());

            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(actualizarProductoRequestJson()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void actualizar_mesero_retorna_403() throws Exception {
            mockMvc.perform(put(BASE_URL + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(actualizarProductoRequestJson()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/productos/{id}")
    class DesactivarProductoTests {

        @Test
        @DisplayName("ADMIN → 204 No Content (soft delete)")
        @WithMockUser(roles = "ADMIN")
        void desactivar_admin_retorna_204() throws Exception {
            doNothing().when(productoService).desactivarProducto(1L);

            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("MESERO → 403 Forbidden")
        @WithMockUser(roles = "MESERO")
        void desactivar_mesero_retorna_403() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
        }
    }
}
