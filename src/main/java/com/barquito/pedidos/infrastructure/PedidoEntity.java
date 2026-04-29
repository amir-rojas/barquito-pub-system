package com.barquito.pedidos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

/**
 * Entidad JPA que mapea la tabla {@code pedidos}.
 *
 * <p>Los campos enum ({@code estado}) se almacenan como {@code String}.
 * La conversión a los enums de dominio se realiza en {@link PedidoJpaAdapter}.
 * Las FKs se almacenan como {@code Long} (FK plana), sin {@code @ManyToOne}.
 */
@Entity
@Table(name = "pedidos")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mesa_id", nullable = false)
    private Long mesaId;

    @Column(name = "mesero_id", nullable = false)
    private Long meseroId;

    @Column(nullable = false)
    private String estado;

    private String notas;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @Column(name = "cerrado_en")
    private OffsetDateTime cerradoEn;

    /** Constructor sin args requerido por JPA. */
    protected PedidoEntity() {}

    /**
     * Construye la entidad con todos sus campos.
     *
     * @param id            identificador.
     * @param mesaId        id de la mesa.
     * @param meseroId      id del mesero.
     * @param estado        estado como texto.
     * @param notas         notas opcionales.
     * @param creadoEn      timestamp de creación.
     * @param actualizadoEn timestamp de actualización.
     * @param cerradoEn     timestamp de cierre/cancelación (puede ser null).
     */
    public PedidoEntity(final Long id, final Long mesaId, final Long meseroId,
                        final String estado, final String notas,
                        final OffsetDateTime creadoEn, final OffsetDateTime actualizadoEn,
                        final OffsetDateTime cerradoEn) {
        this.id = id;
        this.mesaId = mesaId;
        this.meseroId = meseroId;
        this.estado = estado;
        this.notas = notas;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.cerradoEn = cerradoEn;
    }

    /**
     * Inicializa los timestamps en la primera persistencia.
     */
    @PrePersist
    void prePersist() {
        final OffsetDateTime ahora = OffsetDateTime.now();
        if (creadoEn == null) {
            creadoEn = ahora;
        }
        if (actualizadoEn == null) {
            actualizadoEn = ahora;
        }
    }

    /**
     * Actualiza el timestamp en cada modificación.
     */
    @PreUpdate
    void preUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }

    /** @return identificador único del pedido. */
    public Long getId() { return id; }

    /** @return id de la mesa. */
    public Long getMesaId() { return mesaId; }

    /** @return id del mesero. */
    public Long getMeseroId() { return meseroId; }

    /** @return estado como texto plano. */
    public String getEstado() { return estado; }

    /** @return notas opcionales. */
    public String getNotas() { return notas; }

    /** @return timestamp de creación. */
    public OffsetDateTime getCreadoEn() { return creadoEn; }

    /** @return timestamp de última actualización. */
    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }

    /** @return timestamp de cierre/cancelación, o null si ABIERTO. */
    public OffsetDateTime getCerradoEn() { return cerradoEn; }
}
