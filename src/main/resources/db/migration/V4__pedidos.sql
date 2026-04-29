-- =====================================================================
-- V4: PEDIDOS — módulo de gestión de pedidos
--
-- Convenciones:
--   - TEXT para strings (no VARCHAR(N))
--   - TEXT + CHECK IN (...) para enum-like columns (no CREATE TYPE AS ENUM)
--   - BIGINT GENERATED ALWAYS AS IDENTITY para PKs
--   - NUMERIC para dinero; TIMESTAMPTZ para fechas
--   - Índices explícitos en todas las FKs
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. PEDIDOS
-- ---------------------------------------------------------------------
CREATE TABLE pedidos (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mesa_id         BIGINT NOT NULL REFERENCES mesas(id) ON DELETE RESTRICT,
    mesero_id       BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    estado          TEXT NOT NULL DEFAULT 'ABIERTO'
                    CHECK (estado IN ('ABIERTO', 'CERRADO', 'CANCELADO')),
    notas           TEXT,
    creado_en       TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMPTZ NOT NULL DEFAULT now(),
    cerrado_en      TIMESTAMPTZ,
    -- cerrado_en debe ser NULL cuando ABIERTO, y NOT NULL cuando CERRADO o CANCELADO
    CONSTRAINT pedidos_cerrado_en_check
        CHECK (
            (estado = 'ABIERTO' AND cerrado_en IS NULL)
            OR (estado IN ('CERRADO', 'CANCELADO') AND cerrado_en IS NOT NULL)
        )
);

-- Índices en FKs y consultas frecuentes
CREATE INDEX pedidos_mesa_idx ON pedidos (mesa_id);
CREATE INDEX pedidos_mesero_idx ON pedidos (mesero_id);
CREATE INDEX pedidos_estado_idx ON pedidos (estado);
-- Índice parcial: pedidos abiertos por mesa (hot path en crearPedido / cerrarPedido)
CREATE INDEX pedidos_mesa_abierto_idx ON pedidos (mesa_id) WHERE estado = 'ABIERTO';
-- Índice para ordenar por fecha
CREATE INDEX pedidos_creado_en_idx ON pedidos (creado_en DESC);

-- ---------------------------------------------------------------------
-- 2. LINEAS_PEDIDO
-- ---------------------------------------------------------------------
CREATE TABLE lineas_pedido (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id        BIGINT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id      BIGINT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad         NUMERIC(12, 3) NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal         NUMERIC(14, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    estado           TEXT NOT NULL DEFAULT 'PENDIENTE'
                     CHECK (estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO', 'ENTREGADO', 'CANCELADO')),
    notas            TEXT,
    creado_en        TIMESTAMPTZ NOT NULL DEFAULT now(),
    actualizado_en   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índices en FKs y consultas frecuentes
CREATE INDEX lineas_pedido_pedido_idx ON lineas_pedido (pedido_id);
CREATE INDEX lineas_pedido_producto_idx ON lineas_pedido (producto_id);
CREATE INDEX lineas_pedido_estado_idx ON lineas_pedido (estado);
-- Índice parcial para la cola de cocina (estados activos, ordenados por creación)
CREATE INDEX lineas_pedido_cocina_idx ON lineas_pedido (estado, creado_en)
    WHERE estado IN ('PENDIENTE', 'EN_PREPARACION');
-- Índice parcial para líneas activas de un pedido
CREATE INDEX lineas_pedido_pedido_activas_idx ON lineas_pedido (pedido_id)
    WHERE estado IN ('PENDIENTE', 'EN_PREPARACION', 'LISTO');
