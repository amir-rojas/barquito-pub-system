package com.barquito.pedidos.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Entidad JPA que mapea la tabla {@code lineas_pedido}.
 *
 * <p>El campo {@code subtotal} está definido en la base de datos como
 * {@code GENERATED ALWAYS AS (cantidad * precio_unitario) STORED}.
 * Se mapea con {@link Generated} + {@code insertable=false, updatable=false}
 * para que Hibernate lo re-lea después de cada INSERT/UPDATE.
 */
@Entity
@Table(name = "lineas_pedido")
public class LineaPedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(nullable = false, precision = 14, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;

    @Column(nullable = false)
    private String estado;

    private String notas;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    /** Constructor sin args requerido por JPA. */
    protected LineaPedidoEntity() {}

    /**
     * Construye la entidad con todos sus campos.
     *
     * @param id             identificador.
     * @param pedidoId       id del pedido.
     * @param productoId     id del producto.
     * @param cantidad       cantidad pedida.
     * @param precioUnitario precio unitario snapshot.
     * @param estado         estado como texto.
     * @param notas          notas opcionales.
     * @param creadoEn       timestamp de creación.
     * @param actualizadoEn  timestamp de actualización.
     */
    public LineaPedidoEntity(final Long id, final Long pedidoId, final Long productoId,
                              final BigDecimal cantidad, final BigDecimal precioUnitario,
                              final String estado, final String notas,
                              final OffsetDateTime creadoEn, final OffsetDateTime actualizadoEn) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.estado = estado;
        this.notas = notas;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
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

    /** @return identificador único de la línea. */
    public Long getId() { return id; }

    /** @return id del pedido. */
    public Long getPedidoId() { return pedidoId; }

    /** @return id del producto. */
    public Long getProductoId() { return productoId; }

    /** @return cantidad pedida. */
    public BigDecimal getCantidad() { return cantidad; }

    /** @return precio unitario snapshot. */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    /** @return subtotal generado por la BD (puede ser null antes del primer flush). */
    public BigDecimal getSubtotal() { return subtotal; }

    /** @return estado como texto plano. */
    public String getEstado() { return estado; }

    /** @return notas opcionales. */
    public String getNotas() { return notas; }

    /** @return timestamp de creación. */
    public OffsetDateTime getCreadoEn() { return creadoEn; }

    /** @return timestamp de última actualización. */
    public OffsetDateTime getActualizadoEn() { return actualizadoEn; }
}
