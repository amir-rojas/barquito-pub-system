-- =====================================================================
-- V3: MESAS + ZONAS — rediseño completo del módulo de mesas
--
-- Estrategia: ALTER TABLE sobre la tabla mesas existente (no DROP/CREATE)
-- porque ventas.mesa_id referencia mesas.id via FK con ON DELETE RESTRICT.
--
-- Convenciones:
--   - TEXT para strings (no VARCHAR(N))
--   - TEXT + CHECK IN (...) para enum-like columns (no CREATE TYPE AS ENUM)
--   - BIGINT GENERATED ALWAYS AS IDENTITY para PKs nuevas
--   - Índices explícitos en todas las FKs
--   - BOOLEAN NOT NULL con DEFAULT
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. ZONAS — nueva tabla
-- ---------------------------------------------------------------------
CREATE TABLE zonas (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      TEXT NOT NULL
                CHECK (char_length(nombre) BETWEEN 1 AND 100),
    descripcion TEXT,
    orden       INTEGER NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX zonas_nombre_lower_idx ON zonas (LOWER(nombre));

-- Zona por defecto para migrar registros existentes de mesas
-- (zona_id en mesas es NOT NULL → necesitamos al menos una zona antes de ALTER)
INSERT INTO zonas (nombre, descripcion, orden)
VALUES ('Salón principal', 'Zona por defecto creada en migración', 0);

-- ---------------------------------------------------------------------
-- 2. MESAS — rework de la tabla existente
--
-- Cambios respecto a V1:
--   a) Eliminar columna capacidad (decisión de negocio: no se usa)
--   b) Renombrar numero_mesa INTEGER → numero TEXT
--   c) Actualizar CHECK de estado a mayúsculas y agregar FUSIONADA
--   d) Agregar zona_id BIGINT NOT NULL REFERENCES zonas(id)
--   e) Agregar forma TEXT CHECK
--   f) Agregar mesa_principal_id self-FK nullable DEFERRABLE
-- ---------------------------------------------------------------------

-- 2a. Eliminar capacidad
ALTER TABLE mesas DROP COLUMN IF EXISTS capacidad;

-- 2b. Renombrar numero_mesa (INTEGER) → numero (TEXT)
--     Dropear el CHECK (numero_mesa > 0) antes de cambiar el tipo:
--     PostgreSQL re-evalúa los CHECKs con el nuevo tipo y falla con TEXT > INTEGER.
ALTER TABLE mesas DROP CONSTRAINT IF EXISTS mesas_numero_mesa_check;

ALTER TABLE mesas
    ALTER COLUMN numero_mesa TYPE TEXT
    USING numero_mesa::TEXT;

ALTER TABLE mesas RENAME COLUMN numero_mesa TO numero;

-- 2c. El CHECK original de estado cubría ('disponible','ocupada','cuenta_pedida').
--     Necesitamos: ('DISPONIBLE','OCUPADA','CUENTA_PEDIDA','FUSIONADA') en mayúsculas.
--     Primero migrar datos existentes a mayúsculas.
UPDATE mesas SET estado = UPPER(estado);

-- Eliminar el CHECK anterior (nombre generado automáticamente por Postgres en V1)
ALTER TABLE mesas DROP CONSTRAINT IF EXISTS mesas_estado_check;

-- Agregar nuevo CHECK con valores en mayúsculas + FUSIONADA
ALTER TABLE mesas
    ADD CONSTRAINT mesas_estado_check
    CHECK (estado IN ('DISPONIBLE', 'OCUPADA', 'CUENTA_PEDIDA', 'FUSIONADA'));

-- Actualizar DEFAULT de estado a mayúsculas
ALTER TABLE mesas ALTER COLUMN estado SET DEFAULT 'DISPONIBLE';

-- 2d. Agregar zona_id — primero con DEFAULT para satisfacer NOT NULL en filas existentes
ALTER TABLE mesas ADD COLUMN zona_id BIGINT DEFAULT 1 REFERENCES zonas(id) ON DELETE RESTRICT;

-- Asignar zona por defecto a todas las mesas existentes
UPDATE mesas SET zona_id = 1 WHERE zona_id IS NULL;

-- Quitar el DEFAULT (zona_id debe proveerse explícitamente en adelante)
ALTER TABLE mesas ALTER COLUMN zona_id SET NOT NULL;
ALTER TABLE mesas ALTER COLUMN zona_id DROP DEFAULT;

-- 2e. Agregar forma
ALTER TABLE mesas
    ADD COLUMN forma TEXT
    CHECK (forma IN ('CIRCULAR', 'RECTANGULAR'));

-- 2f. Agregar self-FK mesa_principal_id (nullable — solo FUSIONADA lo tiene)
--     DEFERRABLE INITIALLY DEFERRED permite insertar primero la principal y luego la secundaria
--     en la misma transacción sin violar la FK.
ALTER TABLE mesas
    ADD COLUMN mesa_principal_id BIGINT
    REFERENCES mesas(id) ON DELETE RESTRICT
    DEFERRABLE INITIALLY DEFERRED;

-- ---------------------------------------------------------------------
-- 3. ÍNDICES explícitos en FKs (Postgres no auto-indexa FKs)
-- ---------------------------------------------------------------------

-- Índice en zona_id (lookup por zona)
CREATE INDEX mesas_zona_idx ON mesas (zona_id);

-- Índice parcial: zona + estado para mesas activas (consulta frecuente del POS)
CREATE INDEX mesas_zona_activa_idx ON mesas (zona_id) WHERE activa = TRUE;

-- Índice parcial en mesa_principal_id — solo filas que son secundarias
CREATE INDEX mesas_principal_idx ON mesas (mesa_principal_id) WHERE mesa_principal_id IS NOT NULL;

-- Índice parcial en estado — solo mesas activas (reemplaza el de V1 que se recrea)
DROP INDEX IF EXISTS mesas_estado_idx;
CREATE INDEX mesas_estado_idx ON mesas (estado) WHERE activa = TRUE;
