package com.barquito.caja.infrastructure;

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
 * Entidad JPA que mapea la tabla {@code ventas}.
 *
 * <p>Los campos enum ({@code estado}, {@code metodo_pago}) se almacenan como {@code String}.
 * La conversión a los enums de dominio se realiza en {@link VentaJpaAdapter}.
 * Las FKs se almacenan como {@code Long} (FK plana), sin {@code @ManyToOne}.
 *
 * <p>Esta entidad es append-only: no hay {@code @PreUpdate} ni columna {@code actualizado_en}.
 * Las transiciones de estado registran sus propios timestamps ({@code pagado_en}/{@code anulado_en}).
 */
@Entity
@Table(name = "ventas")
public class VentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false, unique = true)
    private Long pedidoId;

    @Column(name = "mesa_id", nullable = false)
    private Long mesaId;

    @Column(name = "cajero_id", nullable = false)
    private Long cajeroId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** TEXT en BD; null cuando PENDIENTE/ANULADA. NUNCA usar {@code @Enumerated}. */
    @Column(name = "metodo_pago")
    private String metodoPago;

    /** TEXT en BD; default 'PENDIENTE'. NUNCA usar {@code @Enumerated}. */
    @Column(nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "pagado_en")
    private OffsetDateTime pagadoEn;

    @Column(name = "anulado_en")
    private OffsetDateTime anuladoEn;

    /** Constructor sin args requerido por JPA. */
    protected VentaEntity() {}

    /**
     * Construye la entidad con todos sus campos.
     *
     * @param id         identificador.
     * @param pedidoId   id del pedido (único).
     * @param mesaId     id de la mesa.
     * @param cajeroId   id del cajero.
     * @param total      total de la venta.
     * @param metodoPago método de pago como texto (puede ser null).
     * @param estado     estado como texto.
     * @param creadoEn   timestamp de creación.
     * @param pagadoEn   timestamp de pago (puede ser null).
     * @param anuladoEn  timestamp de anulación (puede ser null).
     */
    public VentaEntity(final Long id, final Long pedidoId, final Long mesaId,
                       final Long cajeroId, final BigDecimal total,
                       final String metodoPago, final String estado,
                       final OffsetDateTime creadoEn, final OffsetDateTime pagadoEn,
                       final OffsetDateTime anuladoEn) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.mesaId = mesaId;
        this.cajeroId = cajeroId;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.pagadoEn = pagadoEn;
        this.anuladoEn = anuladoEn;
    }

    /**
     * Inicializa {@code creadoEn} en la primera persistencia si no fue provisto.
     */
    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
    }

    /** @return identificador único de la venta. */
    public Long getId() { return id; }

    /** @return id del pedido asociado (único). */
    public Long getPedidoId() { return pedidoId; }

    /** @return id de la mesa. */
    public Long getMesaId() { return mesaId; }

    /** @return id del cajero. */
    public Long getCajeroId() { return cajeroId; }

    /** @return total de la venta. */
    public BigDecimal getTotal() { return total; }

    /** @return método de pago como texto plano (puede ser null). */
    public String getMetodoPago() { return metodoPago; }

    /** @return estado como texto plano. */
    public String getEstado() { return estado; }

    /** @return timestamp de creación. */
    public OffsetDateTime getCreadoEn() { return creadoEn; }

    /** @return timestamp de pago, o null si no está pagada. */
    public OffsetDateTime getPagadoEn() { return pagadoEn; }

    /** @return timestamp de anulación, o null si no está anulada. */
    public OffsetDateTime getAnuladoEn() { return anuladoEn; }
}
