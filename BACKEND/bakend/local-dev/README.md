# Entorno local de desarrollo/demo (sin Docker, sin Supabase)

Docker no arranca en este entorno (el motor Linux de Docker Desktop no
levanta) y el proyecto real de Supabase (`auditorias-industriales`) está
pausado por inactividad del plan gratuito. Mientras eso se resuelve, esta
carpeta arma un stack local equivalente:

- **`LocalPostgres.java`** — Postgres embebido (vía `io.zonky.test:embedded-postgres`,
  el mismo mecanismo que usan los tests de integración del proyecto, pero
  sin Docker). Puerto 5432, usuario/clave `postgres`. Los datos viven en
  `local-dev/pgdata/` (gitignored — no se sube, pero sobrevive entre
  reinicios de este entorno porque ya no está en una carpeta temporal).
  La primera vez que corre, inicializa la base y aplica todas las
  migraciones de `FRONTEND/supabase/migrations/`; las veces siguientes
  detecta que ya existen datos y los reutiliza tal cual.
- **`FakeStorage.java`** — stub mínimo de la REST API de Supabase Storage
  (sin dependencias, usa `com.sun.net.httpserver` del JDK). Puerto 9999.
  Los archivos subidos (PDFs de reporte, evidencia) quedan en
  `local-dev/storage/` (también gitignored).

## Cómo levantar todo (en este orden)

```bash
cd BACKEND/bakend/local-dev

# 1. Resolver el classpath una sola vez (usa el pom.xml de esta carpeta)
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
CP=$(cat cp.txt)

# 2. Postgres (déjalo corriendo en segundo plano)
javac -cp "$CP" LocalPostgres.java
java -cp ".;$CP" LocalPostgres
# Espera el log "PG_READY" (y "MIGRATIONS_APPLIED" solo la primera vez)

# 3. Storage simulado (otra terminal / segundo plano)
javac FakeStorage.java
java FakeStorage
# Espera "FAKE_STORAGE_READY"

# 4. Backend — necesita BACKEND/bakend/.env (gitignored, no existe por defecto)
cd ..
# Crear .env con SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/postgres,
# usuario/clave "postgres", SUPABASE_STORAGE_URL=http://localhost:9999/storage/v1,
# SUPABASE_SERVICE_KEY=cualquier-valor, JWT_SECRET=cualquier-string-de-32+-caracteres,
# CORS_ALLOWED_ORIGINS=http://localhost:5173, SPRING_PROFILES_ACTIVE=dev.
# (ver .env.example para la lista completa de variables)
set -a; source .env; set +a
./mvnw spring-boot:run

# 5. Frontend
cd ../../FRONTEND
npm run dev
```

## Usuario de prueba

Ya existe un usuario auditor sembrado en `local-dev/pgdata/`:

- Correo: `auditor@prueba.com`
- Contraseña: `clave1234`

Si por algún motivo hay que recrearlo (base nueva desde cero), el hash
bcrypt se genera con `BCryptPasswordEncoder` (ver
`spring-security-crypto` en `~/.m2`) e insertarlo directo con SQL:

```sql
INSERT INTO usuario (nombre, correo, password_hash, rol)
VALUES ('Auditor de Prueba', 'auditor@prueba.com', '<hash-bcrypt>', 'auditor');
```

## Catálogo real

El catálogo de auditoría (categorías/subcategorías/cuestionarios/preguntas)
ya está migrado desde `FRONTEND/src/data/mockData.ts` con
`FRONTEND/scripts/migrar-catalogo.mjs` (ese script sí es parte permanente
del repo, no de esta carpeta local-dev — es reutilizable contra Supabase
real cuando el proyecto se reactive).

## Cuándo dejar de usar esto

En cuanto el proyecto de Supabase (`auditorias-industriales`,
`aclampqawqlnzawnqqij`) se reactive desde su dashboard, lo natural es
migrar `local-dev/pgdata/` a Supabase real (o volver a correr
`migrar-catalogo.mjs` apuntando allá) y dejar de depender de este stack
local. Ver memoria `supabase-proyecto-real` para el contexto completo.
