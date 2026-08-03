# bakend — Auditorías Industriales SAS

Backend Spring Boot (monolito modular) para la aplicación de auditorías industriales. Ver `DOCS/PROMPT_corregido.md` en la raíz del repo para el contexto completo de arquitectura y alcance.

## Requisitos

- Java 21
- Maven (o usar `./mvnw`)
- Un proyecto Supabase con el esquema aplicado (`FRONTEND/supabase/migrations/*.sql`, en orden)

## Variables de entorno

Ninguna credencial vive en `application.yml` ni en el código. Copiar `.env.example` a `.env` y completar (ver ese archivo para el detalle de cada variable):

| Variable | Descripción |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev`, `test` o `prod` |
| `SPRING_DATASOURCE_URL` | JDBC de Supabase, con `sslmode=require` obligatorio |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Credenciales de la base de datos |
| `DB_POOL_MAX_SIZE` / `DB_POOL_MIN_IDLE` | Tamaño del pool HikariCP — ajustar al límite de conexiones del plan de Supabase |
| `JWT_SECRET` | Secreto HS256, mínimo 32 caracteres |
| `JWT_ACCESS_TOKEN_MINUTES` / `JWT_REFRESH_TOKEN_DAYS` | Duración de los tokens |
| `CORS_ALLOWED_ORIGINS` | Origen del frontend (`http://localhost:5173` en dev) |
| `SUPABASE_STORAGE_URL` | Base de la REST API de Supabase Storage (`.../storage/v1`) |
| `SUPABASE_SERVICE_KEY` | Service role key de Supabase (nunca la anon key) |
| `SUPABASE_BUCKET_REPORTES` / `SUPABASE_BUCKET_EVIDENCIA` | Buckets de Storage para PDFs y fotos |

Si se despliega más de una instancia del backend en paralelo, usar el endpoint del connection pooler de Supabase (Supavisor) en `SPRING_DATASOURCE_URL` en vez de la conexión directa.

## Ejecutar en local

```bash
./mvnw spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html` (deshabilitado en el perfil `prod`).

## Docker

Solo el backend se conteneriza — la base de datos es Supabase, externa y gestionada.

```bash
cp .env.example .env   # completar con los valores reales
docker compose up --build
```

## Pruebas

Las pruebas de integración (`src/test`) levantan un Postgres real con Testcontainers y le aplican las migraciones reales del proyecto antes de correr, para validar contra el esquema real (triggers, checks, índices) en vez de un esquema autogenerado. Requieren Docker corriendo:

```bash
./mvnw test
```

## Primer usuario

No hay registro público. El primer usuario (rol `admin`) se crea con un INSERT directo en Supabase con una contraseña ya hasheada en BCrypt; a partir de ahí, `POST /api/usuarios` (solo `ADMIN`) da de alta al resto.
