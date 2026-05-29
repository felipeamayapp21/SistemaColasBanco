# Sistema de Gestión de Colas Bancarias

Aplicación web moderna para administrar la atención de clientes en un banco con cola prioritaria, turnos en tiempo real, roles de usuario, y reportes gráficos.

## Requisitos previos

- **Java 21 JDK** — [descargar](https://adoptium.net/)
- **Node.js 18+** — [descargar](https://nodejs.org/)
- **PostgreSQL 15+** — [descargar](https://www.postgresql.org/download/)

---

## Inicio rápido (manual)

```bash
# 1. Clonar
git clone https://github.com/felipeamayapp21/SistemaColasBanco.git
cd SistemaColasBanco

# 2. Crear base de datos en PostgreSQL
psql -U postgres -c "CREATE DATABASE banco_db;"
psql -U postgres -d banco_db -f database/schema.sql
psql -U postgres -d banco_db -f database/seed.sql

# 3. (Opcional) Si tu contraseña de PostgreSQL no es 123456789,
#    edita backend/src/main/resources/application.properties
#    linea: spring.datasource.password=TU_CONTRASENA

# 4. Iniciar backend (puerto 8080)
cd backend
.\mvnw.cmd spring-boot:run   # Windows
# ./mvnw spring-boot:run     # Linux/Mac

# 5. Iniciar frontend (otra terminal, puerto 5173)
cd frontend
npm install
npm run dev
```

La app se abre en http://localhost:5173.

### Inicio rápido (Windows — automático)

Solo doble clic en `ARRANCAR.bat`. Crea la BD, compila el backend, instala dependencias y lanza todo.

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Frontend | Vue 3 (Composition API) + Vite 8 + Bootstrap 5.3 + Pinia |
| Backend | Java 21 + Spring Boot 3.3.5 + Maven |
| BD | PostgreSQL |
| Tiempo real | WebSocket raw |
| Gráficos | Chart.js 4 |
| Auth | JWT con roles ADMIN / CAJERO |

## Funcionalidades

- **Login** con JWT y roles (ADMIN puede gestionar usuarios, CAJERO solo cola)
- **Registro de clientes** con foto, prioridad por edad (>60), documento único
- **Cola en tiempo real** vía WebSocket (sin polling)
- **Llamar siguiente** con prioridad a adultos mayores
- **Pantalla TV** pública `/sala-espera` con turno actual + próximos
- **Dashboard** con KPIs: en espera, atendidos, tiempo promedio
- **Historial** completo de atenciones
- **Reportes** PDF (OpenPDF) y Excel (Apache POI)
- **Gráficos** Chart.js: atenciones por día, por trámite, por cajero
- **Gestión de usuarios** (ADMIN): crear, cambiar rol, activar/desactivar, eliminar
- **Sidebar responsive** con enlaces según rol

## Inicio rápido

```bash
# Requisitos: Java 21, Node.js 18+, PostgreSQL

# 1. Configurar BD en backend/src/main/resources/application.properties
# 2. Iniciar backend
cd backend
.\mvnw.cmd spring-boot:run

# 3. Iniciar frontend (otra terminal)
cd frontend
npm install
npm run dev
```

O doble clic en `ARRANCAR.bat`.

### Puertos

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:5173 |
| API REST | http://localhost:8080 |

### Credenciales por defecto

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| admin | admin123 | ADMIN |
| cajero1 | admin123 | CAJERO |
| cajero2 | admin123 | CAJERO |
| cajero3 | admin123 | CAJERO |

## Estructura

```
ProyectoBancario/
├── ARRANCAR.bat              # Lanzador automático
├── backend/                   # Spring Boot API :8080
│   └── src/main/java/com/banco/
│       ├── config/            # Security, WebSocket, CORS
│       ├── controllers/       # REST endpoints
│       ├── services/          # Lógica de negocio
│       ├── models/            # JPA entities
│       └── repositories/      # Spring Data JPA
├── frontend/                  # Vue 3 + Vite :5173
│   └── src/
│       ├── views/             # Páginas (Dashboard, Cola, etc.)
│       ├── components/        # Sidebar, Navbar, TablaClientes
│       ├── stores/            # Pinia (auth, turno)
│       ├── services/          # Axios, WebSocket
│       ├── router/            # Rutas con guards por rol
│       └── assets/css/        # Metro UI theme
└── database/                  # schema.sql + seed.sql
```

## Roles

| Rol | Acceso |
|-----|--------|
| ADMIN | Todo: dashboard, cola, clientes, historial, reportes, usuarios |
| CAJERO | Dashboard, cola, clientes, historial — sin acceso a Usuarios |

## API endpoints principales

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| POST | `/api/auth/login` | - | Iniciar sesión |
| GET | `/api/clientes` | CAJERO+ | Listar clientes |
| POST | `/api/clientes` | CAJERO+ | Registrar cliente |
| PUT | `/api/clientes/{id}/atender` | CAJERO+ | Llamar siguiente |
| GET | `/api/clientes/tv` | Público | Estado cola para TV |
| GET | `/api/admin/usuarios` | ADMIN | Listar usuarios |
| POST | `/api/admin/usuarios` | ADMIN | Crear usuario |
| PUT | `/api/admin/usuarios/{id}/rol` | ADMIN | Cambiar rol |
| DELETE | `/api/admin/usuarios/{id}` | ADMIN | Eliminar usuario |
| GET | `/api/historial/stats/*` | CAJERO+ | Estadísticas para gráficos |
| GET | `/api/reportes/pdf` | CAJERO+ | Descargar PDF |
| GET | `/api/reportes/excel` | CAJERO+ | Descargar Excel |

## WebSocket

Endpoint: `ws://localhost:8080/ws/queue`

Mensajes broadcast (JSON):
- `QUEUE_UPDATE` — cambio en la cola (registro, llamado, finalizado, cancelado)

## Licencia

MIT
