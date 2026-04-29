-- =====================================================================
-- V5: CAJA — ventas y detalle_ventas (rework completo)
--
-- Convenciones:
--   - TEXT + CHECK IN (...) para enums (uppercase, alineado V3/V4)
--   - BIGINT GENERATED ALWAYS AS IDENTITY para PKs
--   - NUMERIC(12,2) dinero, NUMERIC(12,3) cantidades
--   - subtotal GENERATED ALWAYS AS STORED en detalle_ventas
--   - total NUMERIC(12,2) plano en ventas, calculado en servicio, inmutable post-insert
--   - Append-only: solo se UPDATE-an la transición de estado y sus timestamps
--   - Índices explícitos en todas las FKs + parciales para hot-paths
-- =====================================================================

-- 1) Drop FKs externas que apuntan a ventas legacy (V1)
--    transacciones_financieras usa FK inline → PostgreSQL auto-nombra como venta_id_fkey
ALTER TABLE IF EXISTS transacciones_financieras
    DROP CONSTRAINT IF EXISTS transacciones_financieras_venta_id_fkey;

--    movimientos_inventario usa FK explícita nombrada
ALTER TABLE IF EXISTS movimientos_inventario
    DROP CONSTRAINT IF EXISTS movimientos_venta_fk;

-- 2) Drop tablas legacy V1 (con CASCADE para eliminar dependencias residuales)
DROP TABLE IF EXISTS detalle_ventas CASCADE;
DROP TABLE IF EXISTS ventas         CASCADE;

-- 3) CREATE TABLE ventas (nueva versión alineada con convenciones V3/V4)
CREATE TABLE ventas (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pedido_id   BIGINT NOT NULL UNIQUE
                REFERENCES pedidos(id) ON DELETE RESTRICT,
    mesa_id     BIGINT NOT NULL
                REFERENCES mesas(id) ON DELETE RESTRICT,
    cajero_id   BIGINT NOT NULL
                REFERENCES usuarios(id) ON DELETE RESTRICT,
    total       NUMERIC(12, 2) NOT NULL CHECK (total >= 0),
    metodo_pago TEXT CHECK (metodo_pago IN ('EFECTIVO', 'QR')),
    estado      TEXT NOT NULL DEFAULT 'PENDIENTE'
                CHECK (estado IN ('PENDIENTE', 'PAGADA', 'ANULADA')),
    creado_en   TIMESTAMPTZ NOT NULL DEFAULT now(),
    pagado_en   TIMESTAMPTZ,
    anulado_en  TIMESTAMPTZ,

    CONSTRAINT ventas_pagada_consistente
        CHECK ((estado = 'PAGADA') = (pagado_en IS NOT NULL AND metodo_pago IS NOT NULL)),
    CONSTRAINT ventas_anulada_consistente
        CHECK ((estado = 'ANULADA') = (anulado_en IS NOT NULL)),
    CONSTRAINT ventas_pendiente_consistente
        CHECK (estado <> 'PENDIENTE'
               OR (pagado_en IS NULL AND anulado_en IS NULL AND metodo_pago IS NULL))
);

CREATE INDEX ventas_mesa_idx          ON ventas (mesa_id);
CREATE INDEX ventas_cajero_idx        ON ventas (cajero_id);
CREATE INDEX ventas_creado_en_idx     ON ventas (creado_en DESC);
CREATE INDEX ventas_pendientes_idx    ON ventas (mesa_id, creado_en) WHERE estado = 'PENDIENTE';
CREATE INDEX ventas_pagadas_fecha_idx ON ventas (pagado_en DESC)     WHERE estado = 'PAGADA';

-- 4) CREATE TABLE detalle_ventas (snapshot al facturar)
CREATE TABLE detalle_ventas (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venta_id         BIGINT NOT NULL
                     REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id      BIGINT NOT NULL
                     REFERENCES productos(id) ON DELETE RESTRICT,
    producto_nombre  TEXT NOT NULL,
    cantidad         NUMERIC(12, 3) NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal         NUMERIC(14, 2)
                     GENERATED ALWAYS AS (cantidad * precio_unitario) STORED
);

CREATE INDEX detalle_ventas_venta_idx    ON detalle_ventas (venta_id);
CREATE INDEX detalle_ventas_producto_idx ON detalle_ventas (producto_id);

-- 5) Re-establecer FKs externas apuntando al nuevo ventas.id
--    Solo se añaden si la columna venta_id existe en la tabla (tablas creadas en V1)
ALTER TABLE movimientos_inventario
    ADD CONSTRAINT movimientos_venta_fk
    FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE RESTRICT;

ALTER TABLE transacciones_financieras
    ADD CONSTRAINT transacciones_financieras_venta_id_fkey
    FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE RESTRICT;
