package com.barquito.autenticacion.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que mapea la tabla {@code usuarios}.
 *
 * <p>El campo {@code rol} se almacena como {@code String} y no como enum JPA.
 * La conversión a {@link com.barquito.autenticacion.domain.Rol} se realiza
 * en el adapter {@link UsuarioJpaAdapter} mediante {@code Rol.fromValue()}.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Rol almacenado como texto plano.
     * Valores posibles: {@code admin}, {@code mesero}, {@code bartender}.
     */
    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private boolean activo;

    /** Constructor sin args requerido por JPA. */
    protected UsuarioEntity() {}

    /**
     * Construye una entidad con todos sus campos.
     *
     * @param id           identificador.
     * @param nombre       nombre de login.
     * @param passwordHash hash BCrypt.
     * @param rol          rol como texto.
     * @param activo       estado de habilitación.
     */
    public UsuarioEntity(final Long id,
                         final String nombre,
                         final String passwordHash,
                         final String rol,
                         final boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
    }

    /**
     * @return identificador único del usuario.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return nombre de login.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * @return hash BCrypt de la contraseña.
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * @return rol como texto plano.
     */
    public String getRol() {
        return rol;
    }

    /**
     * @return {@code true} si el usuario está habilitado.
     */
    public boolean isActivo() {
        return activo;
    }
}
