-- =====================================================================
-- SISTEMA DE GESTIÓN DE PUB-BAR
-- PostgreSQL 15+
-- =====================================================================

-- ---------------------------------------------------------------------
-- ENUMs (solo para valores realmente estables)
-- ---------------------------------------------------------------------
CREATE TYPE unidad_medida AS ENUM ('unidad', 'ml');
CREATE TYPE tipo_transaccion AS ENUM ('ingreso', 'egreso');
CREATE TYPE tipo_movimiento_inventario AS ENUM (
    'venta',         -- salida por venta a cliente
    'compra',        -- entrada por reposición de proveedor
    'ajuste',        -- corrección de inventario (+/-)
    'merma',         -- pérdida por rotura, caducidad, etc.
    'devolucion'     -- entrada por anulación de venta
);

-- ---------------------------------------------------------------------
-- USUARIOS
-- ---------------------------------------------------------------------
CREATE TABLE usuarios (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre        TEXT NOT NULL,
    rol           TEXT NOT NULL
                  CHECK (rol IN ('admin', 'mesero', 'bartender')),
    password_hash TEXT NOT NULL,
    activo        BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en     TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Índice para login rápido (case-insensitive)
CREATE UNIQUE INDEX usuarios_nombre_lower_idx ON usuarios (LOWER(nombre));
CREATE INDEX usuarios_rol_idx ON usuarios (rol) WHERE activo = TRUE;

-- ---------------------------------------------------------------------
-- PRODUCTOS (catálogo: cambia poco, se lee mucho)
-- ---------------------------------------------------------------------
CREATE TABLE productos (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre         TEXT NOT NULL,
    precio_venta   NUMERIC(12, 2) NOT NULL CHECK (precio_venta >= 0),
    stock_minimo   NUMERIC(12, 3) NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    unidad_medida  unidad_medida NOT NULL,
    activo         BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX productos_nombre_lower_idx ON productos (LOWER(nombre));
CREATE INDEX productos_activo_idx ON productos (id) WHERE activo = TRUE;

-- ---------------------------------------------------------------------
-- MOVIMIENTOS DE INVENTARIO (libro mayor append-only)
-- Mejora sobre el modelo original: auditoría completa de stock
-- ---------------------------------------------------------------------
CREATE TABLE movimientos_inventario (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    producto_id  BIGINT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    tipo         tipo_movimiento_inventario NOT NULL,
    -- cantidad POSITIVA = entrada, NEGATIVA = salida
    cantidad     NUMERIC(12, 3) NOT NULL CHECK (cantidad <> 0),
    venta_id     BIGINT,   -- FK opcional, se agrega abajo (circular)
    usuario_id   BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    descripcion  TEXT,
    fecha_hora   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX movimientos_producto_idx ON movimientos_inventario (producto_id, fecha_hora DESC);
CREATE INDEX movimientos_venta_idx ON movimientos_inventario (venta_id) WHERE venta_id IS NOT NULL;
CREATE INDEX movimientos_fecha_idx ON movimientos_inventario (fecha_hora);

-- Vista para consultar stock actual (suma del libro mayor)
CREATE VIEW vista_stock_actual AS
SELECT
    p.id                                   AS producto_id,
    p.nombre,
    p.unidad_medida,
    p.stock_minimo,
    COALESCE(SUM(m.cantidad), 0)           AS stock_actual,
    COALESCE(SUM(m.cantidad), 0) <= p.stock_minimo AS bajo_minimo
FROM productos p
LEFT JOIN movimientos_inventario m ON m.producto_id = p.id
WHERE p.activo = TRUE
GROUP BY p.id;

-- ---------------------------------------------------------------------
-- MESAS
-- ---------------------------------------------------------------------
CREATE TABLE mesas (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numero_mesa INTEGER NOT NULL UNIQUE CHECK (numero_mesa > 0),
    estado      TEXT NOT NULL DEFAULT 'disponible'
                CHECK (estado IN ('disponible', 'ocupada', 'cuenta_pedida')),
    capacidad   INTEGER CHECK (capacidad > 0),
    activa      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX mesas_estado_idx ON mesas (estado) WHERE activa = TRUE;

-- ---------------------------------------------------------------------
-- VENTAS (encabezado del pedido)
-- ---------------------------------------------------------------------
CREATE TABLE ventas (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mesa_id      BIGINT NOT NULL REFERENCES mesas(id) ON DELETE RESTRICT,
    usuario_id   BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    total        NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (total >= 0),
    estado       TEXT NOT NULL DEFAULT 'pendiente'
                 CHECK (estado IN ('pendiente', 'pagada', 'anulada')),
    fecha_hora   TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_pago   TIMESTAMPTZ,
    -- No puede estar 'pagada' sin fecha_pago
    CONSTRAINT ventas_pago_consistente
        CHECK ((estado = 'pagada') = (fecha_pago IS NOT NULL))
);

CREATE INDEX ventas_mesa_idx ON ventas (mesa_id);
CREATE INDEX ventas_usuario_idx ON ventas (usuario_id);
CREATE INDEX ventas_fecha_idx ON ventas (fecha_hora DESC);
-- Índice parcial: las ventas pendientes se consultan constantemente en el POS
CREATE INDEX ventas_pendientes_idx ON ventas (mesa_id, fecha_hora)
    WHERE estado = 'pendiente';

-- Ahora sí: FK circular de movimientos_inventario → ventas
ALTER TABLE movimientos_inventario
    ADD CONSTRAINT movimientos_venta_fk
    FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE RESTRICT;

-- ---------------------------------------------------------------------
-- DETALLE DE VENTAS
-- subtotal es GENERATED para que sea imposible inconsistencia aritmética
-- ---------------------------------------------------------------------
CREATE TABLE detalle_ventas (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    venta_id        BIGINT NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    producto_id     BIGINT NOT NULL REFERENCES productos(id) ON DELETE RESTRICT,
    cantidad        NUMERIC(12, 3) NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12, 2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal        NUMERIC(12, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED
);

CREATE INDEX detalle_ventas_venta_idx ON detalle_ventas (venta_id);
CREATE INDEX detalle_ventas_producto_idx ON detalle_ventas (producto_id);

-- ---------------------------------------------------------------------
-- TRANSACCIONES FINANCIERAS
-- ---------------------------------------------------------------------
CREATE TABLE transacciones_financieras (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo        tipo_transaccion NOT NULL,
    monto       NUMERIC(12, 2) NOT NULL CHECK (monto > 0),
    descripcion TEXT NOT NULL,
    venta_id    BIGINT REFERENCES ventas(id) ON DELETE RESTRICT,
    usuario_id  BIGINT REFERENCES usuarios(id) ON DELETE RESTRICT,
    fecha_hora  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX transacciones_venta_idx ON transacciones_financieras (venta_id)
    WHERE venta_id IS NOT NULL;
CREATE INDEX transacciones_fecha_idx ON transacciones_financieras (fecha_hora DESC);
CREATE INDEX transacciones_tipo_fecha_idx ON transacciones_financieras (tipo, fecha_hora DESC);

-- Regla de negocio: si es 'ingreso' ligado a una venta, la venta debe existir
-- (ya lo hace la FK). Garantizamos que una venta solo tenga UN ingreso asociado:
CREATE UNIQUE INDEX transacciones_venta_ingreso_unico_idx
    ON transacciones_financieras (venta_id)
    WHERE venta_id IS NOT NULL AND tipo = 'ingreso';
