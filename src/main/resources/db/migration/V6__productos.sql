-- V6: Add new columns to productos table for extended catalog management
ALTER TABLE productos
    ADD COLUMN descripcion TEXT,
    ADD COLUMN categoria   TEXT NOT NULL DEFAULT 'OTRO'
                           CHECK (categoria IN ('CERVEZA', 'ESPIRITUOSO', 'GASEOSA', 'OTRO')),
    ADD COLUMN disponible  BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX productos_categoria_idx ON productos (categoria) WHERE activo = TRUE;
CREATE INDEX productos_disponible_idx ON productos (disponible) WHERE activo = TRUE;
