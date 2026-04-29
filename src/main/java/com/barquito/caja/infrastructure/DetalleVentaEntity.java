package com.barquito.caja.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;

/**
 * Entidad JPA que mapea la tabla {@code detalle_ventas}.
 *
 * <p>El campo {@code subtotal} está definido en la base de datos como
 * {@code GENERATED ALWAYS AS (cantidad * precio_unitario) STORED}.
 * Se mapea con {@link Generated} + {@code insertable=false, updatable=false}
 * para que Hibernate lo re-lea después de cada INSERT/UPDATE.
 *
 * <p>Mirrors exactamente el patrón de {@code LineaPedidoEntity.subtotal}.
 */
@Entity
@Table(name = "detalle_ventas")
public class DetalleVentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "venta_id", nullable = false)
    private Long ventaId;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "producto_nombre", nullable = false)
    private String productoNombre;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    /** GENERATED ALWAYS AS (cantidad * precio_unitario) STORED. Read-only. */
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(nullable = false, precision = 14, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;

    /** Constructor sin args requerido por JPA. */
    protected DetalleVentaEntity() {}

    /**
     * Construye la entidad con todos sus campos excepto {@code subtotal} (calculado por BD).
     *
     * @param id             identificador.
     * @param ventaId        id de la venta.
     * @param productoId     id del producto.
     * @param productoNombre snapshot del nombre del producto.
     * @param cantidad       cantidad vendida.
     * @param precioUnitario precio unitario snapshot.
     */
    public DetalleVentaEntity(final Long id, final Long ventaId, final Long productoId,
                              final String productoNombre, final BigDecimal cantidad,
                              final BigDecimal precioUnitario) {
        this.id = id;
        this.ventaId = ventaId;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    /** @return identificador único del detalle. */
    public Long getId() { return id; }

    /** @return id de la venta. */
    public Long getVentaId() { return ventaId; }

    /** @return id del producto. */
    public Long getProductoId() { return productoId; }

    /** @return snapshot del nombre del producto al momento de facturación. */
    public String getProductoNombre() { return productoNombre; }

    /** @return cantidad vendida. */
    public BigDecimal getCantidad() { return cantidad; }

    /** @return precio unitario snapshot. */
    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    /** @return subtotal generado por la BD (puede ser null antes del primer flush). */
    public BigDecimal getSubtotal() { return subtotal; }
}
