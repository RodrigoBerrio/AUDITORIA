# Auditoría Empresarial

Aplicación web full-stack para la gestión de auditorías empresariales, con evaluación jerárquica por categorías, generación de informes progresivos y dashboards ejecutivos.

## ¿Qué resuelve?

Muchas auditorías empresariales se hacen hoy en hojas de cálculo dispersas, sin trazabilidad hasta la respuesta original y sin poder mostrar resultados hasta que todo el proceso termina. Este proyecto modela la auditoría como una estructura jerárquica completa y permite generar resultados e informes **a medida que avanza la evaluación**, sin esperar a que esté 100% completa.

## Estructura del modelo

```
Empresa
 └─ Auditoría
     └─ Categoría
         └─ Subcategoría
             └─ Cuestionario
                 └─ Dimensión / Bloque
                     └─ Pregunta
                         └─ Respuesta
```

Una auditoría puede evaluar una sola categoría, varias, o el alcance completo — el sistema no obliga a evaluarlo todo para producir resultados.

## Funcionalidades principales

- **Evaluación flexible por alcance**: auditorías parciales o completas, por categoría, subcategoría o cuestionario específico.
- **Informes progresivos**: un informe puede generarse con lo evaluado hasta el momento (marcado como *preliminar*) y evoluciona automáticamente a *final* cuando se completa su alcance — sin crear un mecanismo de reporte distinto.
- **Consolidación multinivel**: resultados por dimensión, subcategoría, categoría y consolidado general, todos calculados con la misma lógica (dashboard y PDF comparten una única fuente de resultados).
- **"No evaluado" ≠ "cero"**: los elementos fuera de alcance nunca se cuentan como puntaje mínimo, para no distorsionar los resultados.
- **Dashboard ejecutivo**: puntaje global, subcategorías críticas, semáforos de criticidad (escala de madurez 1–5) y navegación tipo *drill-down* hasta la respuesta y evidencia original.
- **Informes en PDF**: informe de subcategoría, de categoría, consolidado multicategoría e integral, con resumen ejecutivo, hallazgos, recomendaciones y plan de acción priorizado — no es la simple unión de PDFs individuales.
- **Trazabilidad completa**: todo resultado puede rastrearse hasta las respuestas que lo originaron.

## Stack técnico

| Capa | Tecnología |
|---|---|
| Frontend | React, Vite, JavaScript, HTML, CSS |
| Backend | Java, Spring Boot, Maven |
| Base de datos | PostgreSQL (vía Supabase) |
| Contenedores | Docker, Docker Compose |

## Estado del proyecto

En desarrollo activo. La categoría **Mantenimiento** (con subcategorías como Almacén de Repuestos, Cultura de Mantenimiento, Estrategia, Habilidades del Personal, Mediciones y Órdenes de Trabajo) se usa como primera implementación y validación del modelo antes de escalar a categorías adicionales (por ejemplo, Energía o Sostenibilidad).

## Instalación y ejecución

### Opción 1 — Manual

```bash
# Backend (Maven)
cd BACKEND/bakend
cp .env.example .env   # completar variables de entorno (conexión a Supabase, etc.)
./mvnw spring-boot:run
# Disponible en http://localhost:8080, conectado a Supabase

# Frontend (Vite)
cd FRONTEND
npm install
npm run dev
# Disponible en http://localhost:5173
```

### Opción 2 — Docker

```bash
docker-compose up
```

### Acceso a la aplicación (entorno de desarrollo local)

Un usuario de prueba viene sembrado en la base de datos para poder iniciar sesión de inmediato en `http://localhost:5173`:

- **Usuario:** `auditor@prueba.com`
- **Contraseña:** `clave1234`

Documentado en `BACKEND/bakend/local-dev/README.md`.

> Estas credenciales son exclusivamente para desarrollo local y no corresponden a ningún entorno de producción.

## Autor

**Rodrigo Berrío Ramírez** — [github.com/RodrigoBerrio](https://github.com/RodrigoBerrio)
Proyecto desarrollado como parte de su transición de carrera hacia tecnología, con apoyo de herramientas de inteligencia artificial a lo largo del ciclo de desarrollo.
