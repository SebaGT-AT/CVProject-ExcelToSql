# Data Importer Pro

Proyecto de portafolio para importar archivos CSV y XLSX hacia PostgreSQL con validaciones, seguridad JWT, historial, dashboard y reportes exportables.

## Stack

- Backend: Java 21, Spring Boot 3, Spring Security, Spring Data JPA, Hibernate, Flyway, Maven
- Base de datos: PostgreSQL
- Frontend: React, Vite, Bootstrap
- DevOps: Docker, Docker Compose, GitHub Actions

## Funcionalidades actuales

- login con JWT y roles `ADMIN` / `OPERADOR`
- carga real de archivos CSV y XLSX
- validacion por tipo de entidad
- importacion completa, parcial o cancelada por errores criticos
- historial filtrable de importaciones
- dashboard con metricas operativas
- exportacion de reportes en CSV y PDF
- frontend conectado al backend

## Estructura

- `backend`: API REST, seguridad, procesamiento de importaciones y migraciones
- `frontend`: interfaz React conectada a la API
- `docs`: notas funcionales y formatos esperados
- `.github/workflows`: pipeline CI

## Variables de entorno principales

### Backend

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `APP_CORS_ALLOWED_ORIGINS`

### Frontend

Usa `frontend/.env.example` como referencia:

- `VITE_API_BASE_URL`

## Credenciales demo

- `admin@datapro.com` / `Admin123!`
- `operator@datapro.com` / `Operator123!`

Estas credenciales son solo para desarrollo local y demo de portafolio.

## Migraciones

El proyecto usa Flyway. La migracion inicial esta en:

- `backend/src/main/resources/db/migration/V1__initial_schema.sql`

## Formatos de importacion

Los encabezados esperados por entidad estan documentados en:

- `docs/import-formats.md`

## Ejecucion local

### Backend

```bash
cd backend
mvn spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

### Docker Compose

```bash
docker-compose up --build
```

## Estado del proyecto

El proyecto ya cuenta con una base full stack funcional y esta entrando en etapa de pulido para portfolio.
