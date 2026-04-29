package com.barquito.productos;

import com.barquito.productos.api.ActualizarProductoRequest;
import com.barquito.productos.api.CrearProductoRequest;
import com.barquito.productos.application.ProductoResponse;
import com.barquito.productos.domain.CategoriaProducto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@code productos} bounded context.
 *
 * <p>Requires Docker. Skipped gracefully if Docker is unavailable via
 * {@code @Testcontainers(disabledWithoutDocker = true)}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@TestPropertySource(properties = {
        "jwt.secret=dGhpcy1pcy1hLWRldi1zZWNyZXQtdGhhdC1pcy0yNTYtYml0cy1sb25nLW9rYXk=",
        "jwt.expiration-ms=28800000"
})
class ProductoControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/api/productos";

    private String crearProductoJson(final String nombre) throws Exception {
        return objectMapper.writeValueAsString(new CrearProductoRequest(
                nombre,
                BigDecimal.valueOf(350.00),
                "Descripción de prueba",
                CategoriaProducto.CERVEZA,
                true
        ));
    }

    private String actualizarProductoJson(final String nombre) throws Exception {
        return objectMapper.writeValueAsString(new ActualizarProductoRequest(
                nombre,
                BigDecimal.valueOf(400.00),
                "Descripción actualizada",
                CategoriaProducto.ESPIRITUOSO,
                false
        ));
    }

    @Test
    @DisplayName("flujo completo: crear → listar → obtener → actualizar → desactivar")
    @WithMockUser(roles = "ADMIN")
    void flujoCompleto_admin() throws Exception {
        final String nombre = "Cerveza IT-" + System.nanoTime();

        // POST → 201 Created
        final MvcResult crearResult = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearProductoJson(nombre)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value(nombre))
                .andExpect(jsonPath("$.categoria").value("CERVEZA"))
                .andExpect(jsonPath("$.disponible").value(true))
                .andExpect(jsonPath("$.activo").value(true))
                .andReturn();

        final ProductoResponse created = objectMapper.readValue(
                crearResult.getResponse().getContentAsString(), ProductoResponse.class);
        assertThat(created.id()).isNotNull();

        // GET / → lista contiene el nuevo producto
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == '" + nombre + "')]").exists());

        // GET /disponibles → contiene el producto
        mockMvc.perform(get(BASE_URL + "/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre == '" + nombre + "')]").exists());

        // GET /{id} → 200
        mockMvc.perform(get(BASE_URL + "/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.id()));

        // PUT /{id} → 200 actualizado
        mockMvc.perform(put(BASE_URL + "/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actualizarProductoJson("Cerveza Premium IT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Cerveza Premium IT"))
                .andExpect(jsonPath("$.categoria").value("ESPIRITUOSO"))
                .andExpect(jsonPath("$.disponible").value(false));

        // DELETE /{id} → 204 (soft delete)
        mockMvc.perform(delete(BASE_URL + "/" + created.id()))
                .andExpect(status().isNoContent());

        // GET /disponibles → ya no contiene el producto (inactivo)
        mockMvc.perform(get(BASE_URL + "/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + created.id() + ")]").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/productos sin token → 401")
    void listar_sinToken_retorna401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/productos con rol MESERO → 403")
    @WithMockUser(roles = "MESERO")
    void crear_mesero_retorna_403() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearProductoJson("Producto-" + System.nanoTime())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/productos con rol MESERO → 200")
    @WithMockUser(roles = "MESERO")
    void listar_mesero_retorna_200() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/productos/{id} not found → 404")
    @WithMockUser(roles = "ADMIN")
    void obtener_not_found_retorna_404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST nombre duplicado → 409 Conflict")
    @WithMockUser(roles = "ADMIN")
    void crear_nombre_duplicado_retorna_409() throws Exception {
        final String nombre = "Producto-Dup-" + System.nanoTime();
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearProductoJson(nombre)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(crearProductoJson(nombre)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE con rol MESERO → 403")
    @WithMockUser(roles = "MESERO")
    void desactivar_mesero_retorna_403() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT con rol MESERO → 403")
    @WithMockUser(roles = "MESERO")
    void actualizar_mesero_retorna_403() throws Exception {
        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(actualizarProductoJson("Nombre")))
                .andExpect(status().isForbidden());
    }
}
