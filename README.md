# Bar-quito — Sistema POS para Pub-Bar

Sistema de punto de venta para el pub familiar **Bar-quito**. Gestión de mesas, pedidos, cobros, inventario de productos y reportes financieros con transacciones ACID.

## Stack

| Capa | Tecnología |
|------|-----------|
| Backend | Java 21 + Spring Boot 3.5.x + Maven |
| Base de datos | PostgreSQL 15+ |
| Migraciones | Flyway |
| Auth | JWT (spring-security-oauth2-resource-server) |
| Tests | JUnit 5 + Mockito + AssertJ + Testcontainers |

## Arquitectura

**Screaming Architecture + Hexagonal** — la estructura refleja el dominio del negocio, no la tecnología.

```
com.barquito/
├── autenticacion/   ← Login, JWT, roles
├── mesas/           ← Estados de mesas, zonas
├── pedidos/         ← Toma de comandas, líneas de pedido
├── caja/            ← Cobros, Ventas ACID
├── productos/       ← Catálogo de productos
├── finanzas/        ← Transacciones financieras, egresos manuales
├── reportes/        ← Analytics y reportes para el ADMIN
└── shared/          ← Seguridad, excepciones globales
```

Cada módulo sigue la misma estructura interna:

```
{modulo}/
├── api/             ← Controllers, requests, responses
├── application/     ← Services, ports (interfaces), commands
├── domain/          ← Entities (records), repositories (interfaces)
└── infrastructure/  ← JPA adapters, entities, Spring Data repos
```

Los módulos se comunican exclusivamente a través de **puertos de anti-corrupción** (interfaces en `application/`), nunca por acceso directo entre capas de infraestructura.

## Bounded Contexts

| Módulo | Estado | Descripción |
|--------|--------|-------------|
| `autenticacion` | ✅ | Login por nombre+contraseña, JWT, roles ADMIN/MESERO |
| `mesas` | ✅ | Mesa FSM: DISPONIBLE → OCUPADA → CUENTA_PEDIDA; zonas |
| `pedidos` | ✅ | Pedido + LineaPedido con FSMs separadas; pessimistic locking |
| `caja` | ✅ | Venta PENDIENTE → PAGADA\|ANULADA; métodos EFECTIVO\|QR |
| `productos` | ✅ | CRUD catálogo; categorías CERVEZA/ESPIRITUOSO/GASEOSA/OTRO |
| `finanzas` | ✅ | Ingresos automáticos post-cobro; egresos manuales ADMIN |
| `reportes` | ✅ | Ventas diarias, top productos, por categoría, resumen período |

**Total: 304 tests passing, 0 failures** (22 skipped — requieren Docker/Testcontainers)

## API — Endpoints principales

### Autenticación
```
POST /api/auth/login
```

### Mesas
```
GET    /api/mesas
POST   /api/mesas                    (ADMIN)
GET    /api/mesas/{id}
PUT    /api/mesas/{id}/estado
DELETE /api/mesas/{id}               (ADMIN)
```

### Pedidos
```
POST   /api/pedidos                  (ADMIN, MESERO)
GET    /api/pedidos/{id}
PUT    /api/pedidos/{id}/estado
POST   /api/pedidos/{id}/lineas
PUT    /api/pedidos/{id}/lineas/{lineaId}
DELETE /api/pedidos/{id}/lineas/{lineaId}
```

### Caja
```
POST   /api/ventas                   crear venta desde pedido CERRADO
PUT    /api/ventas/{id}/cobrar       PENDIENTE → PAGADA
PUT    /api/ventas/{id}/anular       PENDIENTE → ANULADA
GET    /api/ventas/{id}
```

### Productos
```
GET    /api/productos                (ADMIN, MESERO)
GET    /api/productos/disponibles    (ADMIN, MESERO)
GET    /api/productos/{id}           (ADMIN, MESERO)
POST   /api/productos                (ADMIN)
PUT    /api/productos/{id}           (ADMIN)
DELETE /api/productos/{id}           (ADMIN — soft delete)
```

### Finanzas
```
POST   /api/finanzas/egresos         (ADMIN)
GET    /api/finanzas/transacciones   (ADMIN) ?tipo=INGRESO|EGRESO
GET    /api/finanzas/resumen         (ADMIN) ?desde=&hasta=
```

### Reportes
```
GET    /api/reportes/ventas-diarias         (ADMIN) ?fecha=YYYY-MM-DD
GET    /api/reportes/top-productos          (ADMIN) ?desde=&hasta=&limit=10
GET    /api/reportes/ventas-por-categoria   (ADMIN) ?desde=&hasta=
GET    /api/reportes/resumen                (ADMIN) ?desde=&hasta=
```

## Roles

| Rol | Descripción |
|-----|-------------|
| `ADMIN` | Acceso total: CRUD de mesas, productos, reportes, egresos, anulaciones |
| `MESERO` | Operativo: crear pedidos, gestionar líneas, cobrar, ver productos |

## Migraciones Flyway

| Versión | Descripción |
|---------|-------------|
| `V1` | Schema inicial — usuarios, mesas, productos, ventas, transacciones |
| `V2` | Seed de usuarios (admin/mesero iniciales) |
| `V3` | Rework de zonas — parent_id, profundidad, activo |
| `V4` | Pedidos y líneas de pedido (append-only) |
| `V5` | Caja — drop y recreación de ventas/detalle_ventas con CHECK + GENERATED subtotal |
| `V6` | Productos — agrega columnas descripcion, categoria, disponible |

**Regla**: nunca editar una migración ya aplicada. Crear siempre `V{N+1}`.

## Levantar el backend

### Prerrequisitos

- Java 21+
- PostgreSQL 15+

### Setup

```sql
CREATE DATABASE barquito_dev;
```

```bash
cd backend

# Linux / macOS
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Windows PowerShell
./mvnw spring-boot:run "-Dspring.profiles.active=dev"
```

Flyway aplica el schema automáticamente en el primer arranque.

```
GET http://localhost:8080/actuator/health  →  {"status":"UP"}
```

El datasource en `dev` apunta a `localhost:5432/barquito_dev` con usuario `postgres`. Configurable en `backend/src/main/resources/application-dev.yaml`.

### Correr tests

```bash
cd backend
./mvnw test
```

Los tests de integración que requieren Docker se omiten automáticamente si Docker no está disponible (`@Testcontainers(disabledWithoutDocker = true)`).

## Convenciones clave

- **`ddl-auto: validate`** en todos los perfiles — Flyway es el único dueño del schema. Hibernate nunca modifica la DB.
- **TEXT + CHECK** para enums de dominio, no `CREATE TYPE ... AS ENUM` para valores que evolucionan.
- **FK como `Long`** en entidades JPA — sin `@ManyToOne`, para mantener la capa de dominio desacoplada de Hibernate.
- **Pessimistic locking** en transiciones críticas de estado (crear pedido, liberar mesa).
- **`@Component("modulo+NombreClase")`** cuando dos módulos definen adapters con el mismo nombre de clase, para evitar `ConflictingBeanDefinitionException`.
