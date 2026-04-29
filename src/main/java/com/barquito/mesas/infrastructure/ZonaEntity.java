package com.barquito.mesas.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que mapea la tabla {@code zonas}.
 */
@Entity
@Table(name = "zonas")
public class ZonaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private int orden;

    /** Constructor sin args requerido por JPA. */
    protected ZonaEntity() {}

    /**
     * Construye una entidad con todos sus campos.
     *
     * @param id          identificador.
     * @param nombre      nombre de la zona.
     * @param descripcion descripción opcional.
     * @param orden       orden de presentación.
     */
    public ZonaEntity(final Long id, final String nombre,
                      final String descripcion, final int orden) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
    }

    /**
     * @return identificador único de la zona.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return nombre de la zona.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return descripción de la zona.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * @return orden de presentación.
     */
    public int getOrden() {
        return orden;
    }
}
