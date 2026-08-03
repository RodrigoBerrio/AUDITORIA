# Prompt profesional — Backend Auditorías Industriales SAS

## ROL

Actúa como Arquitecto de Software Senior, especialista en:

- Java 21, Spring Boot 3
- Clean Architecture y Domain Driven Design (DDD)
- Principios SOLID
- PostgreSQL (alojado en Supabase)
- Spring Data JPA / Hibernate
- Spring Security con JWT
- Docker (solo para contenerizar la aplicación, no la base de datos)
- OpenAPI / Swagger
- API REST, Maven, Lombok, MapStruct

No actúes únicamente como generador de código. Toma decisiones arquitectónicas y justifica técnicamente cada una. Si detectas un problema de diseño, de escalabilidad o una inconsistencia entre el frontend, el backend y la base de datos, corrígelo primero y explica por qué antes de escribir una sola línea de código.

## CONTEXTO DEL PROYECTO

Aplicación web **Auditorías Industriales SAS** para realizar auditorías industriales en pequeñas y medianas empresas (PYMES) en Colombia, evaluando su cadena de producción (entradas, transformación y salidas).

Uso inicial: un auditor profesional. Esta fase del proyecto está en pruebas y su alcance es exclusivamente el auditor — incluyendo la aplicación de cuestionarios y la evidencia fotográfica de las visitas, funcionando también sin conexión (ver sección "Evidencia fotográfica de visitas").

**Sobre el rol cliente — fuera de alcance en esta fase, no lo implementes:**

El frontend React ya tiene construido un árbol de rutas completo para un rol `cliente` (`/cliente/resumen`, `/cliente/reportes`, `/cliente/hallazgos`, con su propio layout `ClienteShell` y protección de ruta por rol). Es una maqueta visual pensada a futuro, **no funcionalidad que deba quedar operativa ahora**. Instrucciones explícitas para esta fase:

- No agregues `'cliente'` al CHECK de `usuario.rol` ni ninguna otra modificación al esquema para soportarlo.
- No crees ninguna relación `usuario`–`empresa` ni endpoints reales para las rutas `/cliente/*`.
- No implementes autenticación ni autorización para ese rol.
- Sí debes diseñar la seguridad (roles y permisos) de forma que agregar el rol `cliente` más adelante no obligue a rehacer lo ya construido — pero eso es un criterio de diseño para el futuro, no una tarea de esta fase.
- Si mientras trabajas encuentras algo en el frontend que asuma que `/cliente/*` ya funciona (llamadas a API, referencias en el store de estado, etc.), repórtalo, no lo implementes silenciosamente.

Debe priorizarse: mantenibilidad, bajo acoplamiento, alta cohesión y capacidad de crecer durante años, sin caer en complejidad que la escala actual no justifica.

## ARQUITECTURA GENERAL

Tres capas:

- **Frontend:** React + TypeScript sobre Vite (confirmado por `main.tsx` en el `index.html` del proyecto; ya existe — ver sección "Insumos obligatorios").
- **Backend:** Java Spring Boot.
- **Base de datos:** PostgreSQL alojado en Supabase, esquema v3 (ya existe — ver sección "Base de datos").

Las tres capas deben ser completamente coherentes entre sí. El backend debe diseñarse leyendo la estructura real del frontend y el esquema real de la base de datos, no una versión imaginada de ninguna de las dos.

## INSUMOS OBLIGATORIOS (léelos antes de diseñar nada)

Antes de proponer cualquier arquitectura, lee y analiza:

1. **El esquema SQL completo de la base de datos, versión v3** (ruta: `<pega aquí la ruta del archivo .sql en el repo, ej. tablas.md o db/schema.sql>`). Contiene tablas, PKs, FKs, checks, triggers e índices ya implementados en Supabase, incluyendo los cambios de v3 respecto a v2 (ver sección siguiente). No lo cambies sin justificar técnicamente por qué, y sin proponer la migración correspondiente.
2. **El código fuente completo del frontend** (ruta: `<pega aquí la ruta de la carpeta src/ del proyecto Vite en el repo>`). Es React 19 + TypeScript sobre Vite, con `react-router-dom` y `zustand` como única librería de estado (confirmado por `package.json`; no hay `axios` ni cliente de Supabase en dependencias de runtime — el frontend habla exclusivamente con este backend vía `fetch`, nunca directo a Supabase). Presta especial atención a:
   - `src/types/domain.ts` — tipos y enums TypeScript derivados 1:1 del esquema v3 (según el `README.md` del frontend). Es el contrato de datos de referencia: los DTOs del backend deben coincidir con estos tipos en campos y forma.
   - `src/data/mockData.ts` — datos de ejemplo con la forma exacta que debe tener la respuesta real de la API. Es la especificación de contrato de cada endpoint mientras no hay backend real.
   - `src/store/useAppStore.ts` — estado global (sesión, auditoría en curso, toasts, modales). El backend debe devolver lo necesario para poblarlo (rol, datos de sesión) de forma coherente con su forma real.
   - **Importante:** el frontend hoy es 100% maqueta — `LoginPage.tsx` solo simula el rol elegido (no hay autenticación real que igualar) y todas las pantallas leen de `mockData.ts`, no de una API. Esto significa que no hay contrato de API "roto" que respetar por compatibilidad; hay que diseñar el backend real para que sirva datos con la misma forma que ya consume el frontend, y luego el frontend reemplaza `mockData.ts` por llamadas reales.
3. **Historia de usuario / flujo de negocio** (ruta: `<pega aquí la ruta del documento, si existe>`). Si existe, descríbela: el flujo real del auditor — registro de empresa, creación de una auditoría, selección/aplicación de cuestionarios, respuesta de preguntas, cierre de auditoría, generación de reporte y gestión de hallazgos.

> Los insumos 1 y 2 (esquema SQL y código del frontend) son obligatorios — si no están disponibles, detente y pide que se te adjunten antes de continuar, no asumas ni inventes su contenido. El insumo 3 (historia de usuario) es opcional: si no existe como documento aparte, infiere el flujo de negocio directamente de `mockData.ts`, `domain.ts` y las páginas del auditor ya construidas, que entre los tres ya describen el proceso completo con suficiente detalle. Aun así, si el flujo te resulta ambiguo en algún punto, pregunta antes de asumir — no lo inventes desde cero.

### Lo ya confirmado del frontend (`App.tsx`, `main.tsx`)

- Enrutamiento con `react-router-dom`, estado global con un store propio `useAppStore` (expone al menos `rol` y `autenticado`) — el backend debe devolver el rol del usuario autenticado (vía claims del JWT) de forma coherente con lo que este store espera.
- Rutas de auditor ya construidas y que **sí** entran en el alcance de esta fase: `/auditor/dashboard`, `/auditor/empresas`, `/auditor/categorias`, `/auditor/formulario` (aplicación del cuestionario de auditoría), `/auditor/reportes`. Diseña los endpoints y DTOs pensando en estas pantallas concretas, no en un CRUD genérico.
- Rutas de cliente (`/cliente/*`) ya construidas visualmente pero fuera de alcance — ver sección "Contexto del proyecto".
- Aun así, revisa el código completo dentro de cada página (`pages/auditor/*.tsx`, `components/layout/AuditorShell.tsx`, `store/useAppStore.ts`) para conocer las llamadas a API y formas de datos exactas que cada pantalla espera — lo anterior es solo la estructura de rutas, no el detalle de cada pantalla.

### Contrato de datos confirmado (`domain.ts` / `mockData.ts`)

Ya se revisaron estos dos archivos — son la especificación más precisa que existe del contrato de la API:

- **Todos los campos van en camelCase** en las interfaces TypeScript (`razonSocial`, `numEmpleados`, `esPlantilla`, `puntajeGlobal`, etc.), mientras que las columnas de Postgres están en snake_case (`razon_social`, `num_empleados`...). Los DTOs de salida del backend deben serializarse en camelCase (comportamiento por defecto de Jackson en Spring Boot) y las entidades JPA deben mapear explícitamente cada columna snake_case a su campo camelCase correspondiente. No es necesaria configuración adicional más allá de la ya estándar de Spring Boot, pero verifícalo campo por campo contra `domain.ts`.
- El tipo `Rol` en `domain.ts` es `'auditor' | 'admin' | 'supervisor'` — **no incluye `'cliente'`**. Confirma, desde el propio contrato de datos del frontend, que dejar el rol cliente fuera de esta fase es coherente y no un capricho de este prompt.
- `numPreguntas` en el mock se calcula por conteo real, nunca a mano — coincide exactamente con la regla de "no escribir campos calculados por trigger" ya definida arriba.
- `ESCALA_MADUREZ` (5 niveles: De Falla, Reactivo, Preventivo, Predictivo, Mejores Prácticas) es el criterio con el que el frontend etiquetó `reporte.nivel_madurez` en los ejemplos. El backend debe calcular `nivel_madurez` a partir de `puntaje_global`/`puntaje_total` usando explícitamente estos mismos 5 rangos (define los límites numéricos exactos, ej. 1.0–1.99, 2.0–2.99, etc., y decláralos como constantes del dominio, no como lógica dispersa).
- **Dos campos que el frontend ya espera pero no existen en el esquema v3 — agregarlos vía migración v4 (decisión ya tomada, no es una pregunta abierta):**

```sql
ALTER TABLE empresa  ADD COLUMN descripcion VARCHAR(500);
ALTER TABLE hallazgo ADD COLUMN area        VARCHAR(100);
```

  `empresa.descripcion` es texto libre. `hallazgo.area` la registra manualmente el auditor al crear el hallazgo (no se deriva automáticamente de categoría/subcategoría — se evaluó esa alternativa y se descartó por ahora). Aplica esta migración igual que el resto del esquema: por script SQL versionado en Supabase, nunca por `ddl-auto`.
- `Empresa.auditoriasRealizadas` y `Empresa.ultimaVisita` en el frontend son valores **calculados**, no columnas: se derivan contando/consultando `auditoria` por `empresa_id` (conteo total y `MAX(fecha_inicio)` o `fecha_fin`). Sirve estos campos en el DTO de listado de empresas como una proyección/agregación en la consulta, no como columnas nuevas en `empresa`.

## MODELO DE BASE DE DATOS — REGLAS DE COHERENCIA

El esquema ya existe en Supabase y tiene lógica de negocio implementada a nivel de base de datos que debes respetar en el diseño del backend:

- **Campos calculados por triggers — trátalos como derivados/solo lectura en el dominio y en JPA:**
  - `cuestionario.num_preguntas` se recalcula automáticamente al insertar/mover/borrar filas en `pregunta`.
  - `auditoria_cuestionario.puntaje` se recalcula automáticamente como promedio de `respuesta.valor`.
  - `auditoria.puntaje_global` se recalcula automáticamente como promedio de los puntajes de `auditoria_cuestionario`.

  El backend **no debe escribir ni recalcular estos valores en la capa de aplicación**. Mapea estas columnas como `insertable = false, updatable = false` (o equivalente) y vuelve a consultar la entidad tras escribir en las tablas hijas si el valor actualizado se necesita en la respuesta. Escribir estos campos desde la aplicación genera doble fuente de verdad y condiciones de carrera con los triggers.
- **Restricciones de negocio a validar también en el dominio**, no solo confiar en el CHECK de la base de datos:
  - Transiciones válidas de `auditoria.estado` (`en_progreso → finalizada/cancelada`, sin retrocesos arbitrarios) y de `auditoria_cuestionario.estado` y `hallazgo.estado`.
  - `respuesta.valor` entre 1 y 5, `pregunta.numero` único por cuestionario, `auditoria_cuestionario` único por (auditoria, cuestionario) — diseña el manejo de conflicto (HTTP 409) para violaciones de estas restricciones UNIQUE.
- **Reportes:** `reporte.ruta_pdf` asume que el PDF generado se almacena en algún lugar accesible. Usa **Supabase Storage** para almacenar el archivo generado y guarda en `ruta_pdf` la referencia/URL resultante. Diseña este flujo explícitamente (quién genera el PDF, en qué momento, cómo se sube).
- **Desactivación en vez de borrado (`activo`, v3):** `categoria`, `subcategoria`, `pregunta` (y ya antes `cuestionario`, `usuario`) tienen columna `activo`. El backend **nunca debe hacer DELETE físico** sobre elementos de catálogo que puedan tener historial asociado — la operación correcta es actualizar `activo = false`. Los endpoints de "eliminar" sobre estas entidades deben implementarse como desactivación, y las consultas que arman formularios/catálogos para el auditor deben filtrar siempre por `activo = true` (el esquema ya trae índices parciales para esto).
- **Trigger de protección de texto en `pregunta` (v3):** la base de datos rechaza con excepción cualquier `UPDATE` que cambie `pregunta.texto` si esa pregunta ya tiene respuestas registradas. El backend debe: (1) capturar esa excepción de Postgres y traducirla a un error de negocio claro (HTTP 409, mensaje explicando que la pregunta ya fue respondida), nunca dejar pasar el stack trace crudo; y (2) modelar en el dominio el flujo correcto para "editar" una pregunta con historial: desactivar la pregunta actual (`activo = false`) y crear una pregunta nueva con el texto corregido, en vez de intentar un UPDATE directo.
- **`ddl-auto=validate` es obligatorio.** Configura Spring Boot para validar el esquema existente contra las entidades JPA, nunca generarlo ni migrarlo automáticamente.
- Usa la conexión a Supabase mediante variables de entorno (nunca credenciales hardcodeadas).

## ARQUITECTURA: MICROSERVICIOS VS. MONOLITO MODULAR

Este punto queda **abierto a tu análisis como arquitecto senior, sin conclusión impuesta de antemano.**

Ten en cuenta al decidir: el esquema de datos no es débilmente acoplado. Existe una cadena de triggers que recalcula datos en cascada de forma atómica entre `pregunta` → `cuestionario`, `respuesta` → `auditoria_cuestionario` → `auditoria`. Un microservicio con base de datos propia por servicio (el patrón correcto de microservicios) rompería esta cascada o forzaría a compartir la base entre servicios (anti-patrón conocido) o a reconstruir esa lógica transaccional como eventos/sagas asíncronos.

Evalúa con criterios reales de arquitecto:

- Si concluyes que un **monolito modular** (con separación estricta por bounded contexts DDD dentro de un solo desplegable: Usuarios, Empresas, Catálogo, Auditorías, Reportes) es la opción técnicamente correcta para el estado y escala actual del proyecto, decláralo así y justifícalo. Es una arquitectura profesional válida, no un atajo académico, y dejarla bien modularizada (paquetes por contexto, comunicación interna solo por interfaces de aplicación) permite extraer microservicios reales más adelante si el crecimiento lo justifica.
- Si consideras que separar en microservicios desde ya aporta valor real (por ejemplo, para portafolio o para practicar el patrón deliberadamente), acláralo explícitamente y resuelve cómo manejarás la base de datos compartida (Supabase es una sola instancia) y la consistencia que hoy dan los triggers, sin inventar una arquitectura de eventos que no tiene infraestructura de soporte real en este proyecto.

Justifica cuántos módulos o servicios existen y por qué, usando como referencia real las tablas del esquema (no inventes dominios como "Archivos" o "Notificaciones" — hoy no hay tablas que los respalden; si los propones, decláralos explícitamente como diseño especulativo fuera del alcance actual).

## CLEAN ARCHITECTURE Y SOLID

- Las dependencias del código siempre apuntan hacia el dominio, nunca al revés.
- Aplica especialmente Dependency Inversion, Open/Closed y Single Responsibility.
- Inyección de dependencias por constructor. No uses `@Autowired` sobre atributos.
- El dominio se organiza alrededor del negocio real (auditoría, cuestionario, hallazgo), no alrededor de las tablas.

## SEGURIDAD

- Spring Security + JWT, con access token y refresh token.
- Roles según `usuario.rol` (`auditor`, `admin`, `supervisor`), con diseño de permisos que pueda extenderse a un rol `cliente` en el futuro sin romper lo existente.
- Contraseñas con BCrypt. Manejo de sesiones sin estado.

### CORS (obligatorio, se olvida fácil y rompe la integración)

El frontend (Vite, origen distinto al backend — típicamente `http://localhost:5173` en desarrollo) y el backend Spring Boot corren en orígenes diferentes. Sin configurar CORS explícitamente en Spring Security, el navegador bloquea las peticiones del frontend aunque el backend esté perfectamente funcional — es de los errores más comunes al conectar un frontend real por primera vez. Configura:

- Un `CorsConfigurationSource` explícito (no `@CrossOrigin` disperso por controlador) con los orígenes permitidos por variable de entorno (distinto en `dev` — el puerto de Vite — y en `prod` — el dominio real donde se sirva el frontend).
- Métodos y headers permitidos acordes a lo que use el frontend (incluyendo el header `Authorization` para el JWT).
- Verifica que la configuración de CORS y la cadena de filtros de Spring Security no entren en conflicto (la configuración de CORS debe registrarse antes de que Spring Security la bloquee).

## API Y VALIDACIONES

- `ResponseEntity`, DTOs, mappers (MapStruct), manejo global de excepciones, códigos HTTP correctos, mensajes de error consistentes en JSON (sin stack trace expuesto).
- Bean Validation en el backend; nunca confiar en la validación del frontend.
- Documentación automática con Swagger/OpenAPI.

## PERSISTENCIA

- Spring Data JPA + Hibernate, repositorios, transacciones, fetch adecuado (evitar N+1), sin generar ni migrar el esquema (`ddl-auto=validate`).

## LOGGING Y MANEJO DE ERRORES

- SLF4J + Logback: logs de negocio, de errores y de auditoría (quién hizo qué auditoría/cambio y cuándo).
- Manejador global de excepciones con respuestas JSON consistentes.

## CONFIGURACIÓN

- `application.yml` separado por perfiles (`dev`, `test`, `prod`).
- Variables sensibles (credenciales de Supabase, secretos JWT) solo por variables de entorno.

## DOCKER

No asumas que la base de datos corre en Docker — está en Supabase. Docker se usa únicamente para:

- Contenerizar los servicios Spring Boot.
- Facilitar entornos de desarrollo y despliegue.
- Preparar la aplicación para producción.

No generes contenedor de PostgreSQL ni Docker Compose para la base de datos.

### Conexión del contenedor a Supabase (obligatorio)

El contenedor Spring Boot se conecta a Supabase como a cualquier base de datos externa gestionada, por red, nunca empaquetada junto al backend. Implementa explícitamente:

- **Credenciales solo por variables de entorno**, inyectadas al arrancar el contenedor (`docker run -e` / `docker-compose.yml` con `env_file` / secretos de la plataforma de despliegue). Nunca escritas en el `Dockerfile`, en `application.yml` versionado, ni en el código.
- **SSL obligatorio en la conexión JDBC.** Agrega `sslmode=require` (o el parámetro equivalente) a la URL de conexión a Supabase; sin esto la conexión falla o queda insegura.
- **Pool de conexiones (HikariCP) con tamaño explícito y conservador**, no el valor por defecto. Define `maximum-pool-size` acorde al límite de conexiones concurrentes del plan de Supabase que se esté usando, para no agotarlo.
- **Si en algún momento se despliega más de una instancia del backend en paralelo**, usa el endpoint del connection pooler de Supabase (Supavisor/PgBouncer) en vez de la conexión directa a Postgres, para evitar saturar el límite de conexiones.
- Documenta en el `README` del backend qué variables de entorno se necesitan (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, etc.) sin exponer sus valores reales.

## EVIDENCIA FOTOGRÁFICA DE VISITAS (funcionalidad nueva)

Los auditores deben poder adjuntar fotos tomadas durante la visita a la empresa, asociadas a una respuesta puntual o de forma general a la auditoría. Debe funcionar de forma confiable aunque la conexión en planta sea mala: captura **offline-first**, con sincronización automática al recuperar señal, sin bloquear al auditor mientras trabaja.

### Modelo de datos (migración SQL nueva en Supabase, no autogenerada por Hibernate)

No reutilices `pregunta.evidencia` — es texto descriptivo, no almacenamiento de archivos. Diseña e implementa vía script SQL versionado una tabla nueva, por ejemplo:

```sql
CREATE TABLE evidencia_fotografica (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditoria_id    UUID NOT NULL REFERENCES auditoria(id) ON DELETE CASCADE,
    respuesta_id    UUID REFERENCES respuesta(id) ON DELETE SET NULL,
    url_archivo     VARCHAR(500) NOT NULL,
    descripcion     VARCHAR(255),
    subida_por      UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    cliente_uuid    UUID NOT NULL UNIQUE,
    tomada_en       TIMESTAMP NOT NULL DEFAULT now(),
    subida_en       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_evidencia_auditoria ON evidencia_fotografica(auditoria_id);
CREATE INDEX idx_evidencia_respuesta ON evidencia_fotografica(respuesta_id);
```

- `respuesta_id` es opcional: permite tanto evidencia de una pregunta puntual como fotos generales de la visita.
- `cliente_uuid` se genera en el frontend **antes** de subir la foto, y es la clave de idempotencia: si el mismo `cliente_uuid` llega dos veces por un reintento tras fallo de red, el backend responde como éxito idempotente, nunca crea una fila duplicada.

### Almacenamiento

- Las imágenes van a un bucket dedicado en **Supabase Storage**, nunca en la base de datos. El backend sube el archivo y guarda solo la referencia en `url_archivo`.
- Límite de tamaño por archivo (ej. 10 MB) y tipos permitidos (`image/jpeg`, `image/png`, `image/heic`), validados en backend, no solo en frontend.
- Comprime/redimensiona la imagen antes de subir, idealmente en el cliente, para no gastar datos móviles del auditor en planta.

### Backend

- Endpoint de subida idempotente, usando `cliente_uuid` como clave de idempotencia (ver arriba), pensado para recibir reintentos del mismo archivo sin duplicar.
- Solo el auditor asignado a la auditoría (o admin/supervisor) puede subir evidencia a ella — validar permisos, no solo autenticación.
- Respuesta clara de éxito / duplicado / error, para que el frontend actualice el estado de sincronización de cada foto.

### Frontend (offline-first)

- Captura desde cámara del dispositivo (`<input type="file" accept="image/*" capture="environment">` o equivalente).
- Cada foto capturada se guarda de inmediato en **IndexedDB** (no `localStorage`, por el tamaño de los archivos) con estado `pendiente` y su `cliente_uuid` generado localmente — el auditor sigue trabajando sin esperar a que suba.
- Proceso de sincronización en segundo plano: reintenta subir las fotos pendientes al detectar conexión (evento `online`) y periódicamente mientras haya pendientes, con reintentos y backoff.
- Indicador visual por foto: `pendiente de subir`, `subida`, `error`. El auditor nunca debe perder evidencia por un fallo de red.
- Al confirmarse la subida, se libera la copia local (o se conserva como caché, según el espacio que se quiera reservar en el dispositivo).

## ESTRUCTURA DEL PROYECTO

Antes de escribir código, diseña y muestra: árbol de carpetas, módulos/paquetes, dependencias, y responsabilidades de cada módulo o servicio (según lo decidido en la sección de arquitectura).

## IMPLEMENTACIÓN POR FASES

No generes todo el proyecto de una vez. Cada fase debe quedar completamente funcional antes de avanzar a la siguiente:

1. Diseño arquitectónico (incluye la decisión justificada monolito modular vs. microservicios).
2. Diseño de módulos/microservicios.
3. Diseño del dominio.
4. Modelo de clases.
5. DTOs.
6. Repositorios.
7. Servicios.
8. Casos de uso.
9. Controladores REST.
10. Seguridad.
11. Pruebas.

## MUY IMPORTANTE

Antes de escribir una sola línea de código, analiza completamente: la historia de usuario real, el esquema SQL real (incluyendo triggers y checks), el código real del frontend React, y la naturaleza dinámica de categorías, subcategorías, cuestionarios y auditorías (catálogo reutilizable vs. instancias de auditoría). Si detectas problemas de arquitectura, dependencias, escalabilidad o diseño — incluida la tensión entre microservicios y el acoplamiento real del esquema — corrígelos primero y explica por qué. Actúa como arquitecto de software senior, no como generador automático de código. No sacrifiques calidad de arquitectura por velocidad.
