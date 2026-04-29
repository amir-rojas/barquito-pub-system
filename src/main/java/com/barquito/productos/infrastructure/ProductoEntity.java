package com.barquito.productos.infrastructure;

import com.barquito.productos.domain.CategoriaProducto;
import com.barquito.productos.domain.Producto;
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
 * Entidad JPA que mapea la tabla {@code productos}.
 *
 * <p>No tiene setters: los campos se asignan en el factory method {@link #toEntity(Producto)}
 * y en el hook {@link #prePersist()}. Sigue el patrón hexagonal: solo existe en la capa
 * de infraestructura y nunca se expone hacia el dominio.
 */
@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "precio_venta", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioVenta;

    @Column(name = "stock_minimo", nullable = false, precision = 12, scale = 3)
    private BigDecimal stockMinimo;

    @Column(name = "unidad_medida", columnDefinition = "unidad_medida")
    private String unidadMedida;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private Boolean disponible;

    @Column(nullable = false)
    private Boolean activo;

    @Column(name = "creado_en", nullable = false)
    private OffsetDateTime creadoEn;

    /** Constructor sin args requerido por JPA. */
    protected ProductoEntity() {}

    /**
     * Hook de JPA que inicializa campos con valores por defecto antes de persistir.
     */
    @PrePersist
    void prePersist() {
        if (creadoEn == null) {
            creadoEn = OffsetDateTime.now();
        }
        if (activo == null) {
            activo = true;
        }
        if (stockMinimo == null) {
            stockMinimo = BigDecimal.ZERO;
        }
        if (unidadMedida == null) {
            unidadMedida = "unidad";
        }
        if (disponible == null) {
            disponible = true;
        }
    }

    /**
     * Construye una {@link ProductoEntity} a partir de un {@link Producto} de dominio.
     *
     * @param producto el objeto de dominio a convertir.
     * @return la entidad JPA correspondiente.
     */
    public static ProductoEntity toEntity(final Producto producto) {
        final ProductoEntity entity = new ProductoEntity();
        entity.id = producto.id();
        entity.nombre = producto.nombre();
        entity.precioVenta = producto.precio();
        entity.descripcion = producto.descripcion();
        entity.categoria = producto.categoria() != null
                ? producto.categoria().name()
                : CategoriaProducto.OTRO.name();
        entity.disponible = producto.disponible();
        entity.activo = producto.activo();
        entity.creadoEn = producto.creadoEn();
        entity.stockMinimo = BigDecimal.ZERO;
        entity.unidadMedida = "unidad";
        return entity;
    }

    /**
     * Convierte esta entidad JPA en un objeto de dominio {@link Producto}.
     *
     * @return el {@link Producto} de dominio correspondiente.
     */
    public Producto toDomain() {
        return new Producto(
                id,
                nombre,
                precioVenta,
                descripcion,
                categoria != null ? CategoriaProducto.valueOf(categoria) : CategoriaProducto.OTRO,
                Boolean.TRUE.equals(disponible),
                Boolean.TRUE.equals(activo),
                creadoEn
        );
    }

    /** @return identificador único de la entidad. */
    public Long getId() { return id; }

    /** @return nombre del producto. */
    public String getNombre() { return nombre; }

    /** @return precio de venta. */
    public BigDecimal getPrecioVenta() { return precioVenta; }

    /** @return categoría como String. */
    public String getCategoria() { return categoria; }

    /** @return true si el producto está disponible. */
    public Boolean getDisponible() { return disponible; }

    /** @return true si el producto está activo. */
    public Boolean getActivo() { return activo; }
}
