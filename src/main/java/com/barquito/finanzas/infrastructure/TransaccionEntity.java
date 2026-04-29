package com.barquito.finanzas.infrastructure;

import com.barquito.finanzas.domain.TipoTransaccion;
import com.barquito.finanzas.domain.Transaccion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad JPA que mapea la tabla {@code transacciones_financieras}.
 *
 * <p>El campo {@code tipo} es un PG ENUM ({@code tipo_transaccion}) existente en V1;
 * se mapea como {@code String} con {@code columnDefinition = "tipo_transaccion"}.
 * La conversión entre el enum de dominio y el texto lowercase se realiza en
 * {@link TransaccionJpaAdapter}.
 *
 * <p>Las FKs ({@code venta_id}, {@code usuario_id}) se almacenan como {@code Long} planos,
 * sin {@code @ManyToOne}, siguiendo la convención del proyecto.
 */
@Entity
@Table(name = "transacciones_financieras")
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo de transacción. Columna de tipo PG ENUM {@code tipo_transaccion}.
     * Valores: {@code ingreso} o {@code egreso} (siempre en minúsculas).
     */
    @Column(name = "tipo", nullable = false, columnDefinition = "tipo_transaccion")
    private String tipo;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** FK plana a la tabla {@code ventas}. Nullable para egresos manuales. */
    @Column(name = "venta_id")
    private Long ventaId;

    /** FK plana a la tabla {@code usuarios}. */
    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "fecha_hora", nullable = false)
    private OffsetDateTime fechaHora;

    /** Constructor sin args requerido por JPA. */
    protected TransaccionEntity() {}

    /**
     * Construye la entidad con todos sus campos.
     *
     * @param id          identificador.
     * @param tipo        tipo como texto lowercase ({@code ingreso}/{@code egreso}).
     * @param monto       importe de la transacción.
     * @param descripcion descripción textual.
     * @param ventaId     id de la venta (nullable).
     * @param usuarioId   id del usuario (nullable).
     * @param fechaHora   fecha y hora de la transacción (nullable, se asigna en PrePersist).
     */
    public TransaccionEntity(final Long id, final String tipo, final BigDecimal monto,
                              final String descripcion, final Long ventaId, final Long usuarioId,
                              final OffsetDateTime fechaHora) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.descripcion = descripcion;
        this.ventaId = ventaId;
        this.usuarioId = usuarioId;
        this.fechaHora = fechaHora;
    }

    /**
     * Inicializa {@code fechaHora} en la primera persistencia si no fue provisto.
     */
    @PrePersist
    void prePersist() {
        if (fechaHora == null) {
            fechaHora = OffsetDateTime.now();
        }
    }

    /**
     * Crea una {@link TransaccionEntity} desde una entidad de dominio.
     *
     * <p>Convierte el enum {@link TipoTransaccion} a lowercase String
     * (e.g. {@code INGRESO} → {@code "ingreso"}).
     *
     * @param t la transacción de dominio.
     * @return entidad JPA lista para persistir.
     */
    public static TransaccionEntity toEntity(final Transaccion t) {
        return new TransaccionEntity(
                t.id(),
                t.tipo().name().toLowerCase(),
                t.monto(),
                t.descripcion(),
                t.ventaId(),
                t.usuarioId(),
                t.fechaHora()
        );
    }

    /**
     * Convierte esta entidad JPA al objeto de dominio {@link Transaccion}.
     *
     * <p>Convierte el tipo String lowercase al enum
     * (e.g. {@code "ingreso"} → {@code TipoTransaccion.INGRESO}).
     *
     * @return transacción de dominio.
     */
    public Transaccion toDomain() {
        return new Transaccion(
                id,
                TipoTransaccion.valueOf(tipo.toUpperCase()),
                monto,
                descripcion,
                ventaId,
                usuarioId,
                fechaHora
        );
    }

    /** @return identificador único. */
    public Long getId() { return id; }

    /** @return tipo como texto lowercase. */
    public String getTipo() { return tipo; }

    /** @return importe de la transacción. */
    public BigDecimal getMonto() { return monto; }

    /** @return descripción textual. */
    public String getDescripcion() { return descripcion; }

    /** @return id de la venta asociada (puede ser null). */
    public Long getVentaId() { return ventaId; }

    /** @return id del usuario (puede ser null). */
    public Long getUsuarioId() { return usuarioId; }

    /** @return fecha y hora de la transacción. */
    public OffsetDateTime getFechaHora() { return fechaHora; }
}
