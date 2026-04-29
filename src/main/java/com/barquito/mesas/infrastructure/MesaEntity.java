package com.barquito.mesas.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que mapea la tabla {@code mesas}.
 *
 * <p>Los campos {@code estado} y {@code forma} se almacenan como {@code String}
 * y no como enums JPA. La conversión a los enums de dominio se realiza en
 * {@link MesaJpaAdapter} mediante los métodos {@code fromValue()}.
 *
 * <p>El campo {@code zonaId} y {@code mesaPrincipalId} se almacenan como
 * {@code Long} (FK plana) para evitar lazy-loading proxies en el dominio.
 */
@Entity
@Table(name = "mesas")
public class MesaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private boolean activa;

    @Column(name = "zona_id", nullable = false)
    private Long zonaId;

    private String forma;

    @Column(name = "mesa_principal_id")
    private Long mesaPrincipalId;

    /** Constructor sin args requerido por JPA. */
    protected MesaEntity() {}

    /**
     * Construye una entidad con todos sus campos.
     *
     * @param id              identificador.
     * @param numero          número visible de la mesa.
     * @param estado          estado como texto.
     * @param activa          estado de habilitación.
     * @param zonaId          id de la zona.
     * @param forma           forma como texto (puede ser null).
     * @param mesaPrincipalId id de la mesa principal (puede ser null).
     */
    public MesaEntity(final Long id, final String numero, final String estado,
                      final boolean activa, final Long zonaId,
                      final String forma, final Long mesaPrincipalId) {
        this.id = id;
        this.numero = numero;
        this.estado = estado;
        this.activa = activa;
        this.zonaId = zonaId;
        this.forma = forma;
        this.mesaPrincipalId = mesaPrincipalId;
    }

    /**
     * @return identificador único de la mesa.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return número visible de la mesa.
     */
    public String getNumero() {
        return numero;
    }

    /**
     * @return estado como texto plano.
     */
    public String getEstado() {
        return estado;
    }

    /**
     * @return {@code true} si la mesa está habilitada.
     */
    public boolean isActiva() {
        return activa;
    }

    /**
     * @return id de la zona a la que pertenece la mesa.
     */
    public Long getZonaId() {
        return zonaId;
    }

    /**
     * @return forma como texto plano (puede ser null).
     */
    public String getForma() {
        return forma;
    }

    /**
     * @return id de la mesa principal si esta es secundaria (puede ser null).
     */
    public Long getMesaPrincipalId() {
        return mesaPrincipalId;
    }
}
