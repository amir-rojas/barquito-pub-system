package com.barquito.mesas.application;

import com.barquito.mesas.domain.Zona;

import java.util.List;

/**
 * Puerto de entrada (input port) para operaciones sobre zonas.
 *
 * <p>Define el contrato que expone la capa de aplicación al mundo exterior.
 * La implementación concreta es {@code ZonaServiceImpl}.
 */
public interface ZonaService {

    /**
     * Crea una nueva zona.
     *
     * @param nombre      nombre de la zona (único, case-insensitive).
     * @param descripcion descripción opcional.
     * @param orden       orden de presentación.
     * @return la zona creada con id asignado.
     */
    Zona crearZona(String nombre, String descripcion, int orden);

    /**
     * Retorna todas las zonas ordenadas por {@code orden} ascendente.
     *
     * @return lista de zonas.
     */
    List<Zona> listarZonas();

    /**
     * Actualiza los datos de una zona existente.
     *
     * @param id          id de la zona a actualizar.
     * @param nombre      nuevo nombre (único, case-insensitive).
     * @param descripcion nueva descripción.
     * @param orden       nuevo orden.
     * @return la zona actualizada.
     */
    Zona actualizarZona(Long id, String nombre, String descripcion, int orden);
}
