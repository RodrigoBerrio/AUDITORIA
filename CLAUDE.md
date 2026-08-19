# CLAUDE.md

# Proyecto: Aplicación Web de Auditoría Empresarial

## Objetivo

Esta aplicación permite realizar auditorías empresariales mediante una estructura jerárquica de:

Empresa
→ Auditoría
→ Categoría
→ Subcategoría
→ Cuestionario
→ Dimensión/Bloque
→ Pregunta
→ Respuesta

La aplicación debe permitir auditorías parciales o completas.

Una auditoría puede evaluar:
- una categoría;
- varias categorías;
- todas las categorías;
- determinadas subcategorías o cuestionarios.

Los elementos no evaluados NO deben considerarse con puntaje cero.

---

## Principio de arquitectura

El sistema debe ser escalable.

Al agregar nuevas categorías, subcategorías o cuestionarios, debe reutilizarse en lo posible la arquitectura existente.

El sistema de resultados debe permitir generar:

- resultados por cuestionario;
- resultados por subcategoría;
- resultados por categoría;
- resultados consolidados de varias categorías;
- resultado integral cuando se evalúen todas las categorías.

Principio:

Pregunta
→ Dimensión
→ Subcategoría
→ Categoría
→ Consolidado

---

## Dashboard e informes

La aplicación YA contiene código para:

- dashboards;
- gráficos;
- cálculo de resultados;
- generación de informes PDF.

NO construir estas funcionalidades nuevamente sin revisar primero lo existente.

Prioridad obligatoria:

**Reutilizar → Extender → Refactorizar mínimamente → Crear nuevo**

Evitar duplicar:

- componentes;
- cálculos;
- servicios;
- consultas;
- lógica de negocio;
- generadores PDF.

Dashboard y PDF deben utilizar la misma fuente de resultados.

---

## Informes

Cada nivel evaluado debe poder producir resultados independientes.

Debe ser posible generar:

- Informe de Subcategoría
- Informe de Categoría
- Informe Consolidado de varias categorías
- Informe Integral de Auditoría

El alcance real de la auditoría determina el contenido del informe.

**No evaluado ≠ 0**

Un informe consolidado NO debe ser simplemente la unión de varios PDF.

Debe consolidar:

- resultados;
- criticidad;
- principales hallazgos;
- recomendaciones;
- prioridades;
- plan de acción.

---

## Escala de auditoría

La aplicación utiliza principalmente una escala de madurez de:

**1 a 5**

La metodología de cálculo existente NO debe modificarse sin autorización.

Antes de cambiar fórmulas, ponderaciones o rangos, explicar el impacto y solicitar aprobación.

---

## Integridad de datos

No:

- inventar resultados;
- inventar indicadores;
- inventar hallazgos;
- inventar cifras económicas;
- convertir "No aplica" en cero;
- modificar cuestionarios existentes sin autorización.

Los resultados deben ser trazables hasta las respuestas originales.

---

## Documentación funcional

La carpeta `docs/` contiene documentos de referencia del proyecto.

Consultar especialmente:

`docs/PROMPT_DASHBOARD_REPORTES.md`

También pueden existir:

- cuestionarios de auditoría;
- referencias visuales;
- ejemplos de informes;
- esquemas de dashboards.

Los cuestionarios adjuntos son referencia funcional.

NO recrearlos automáticamente si ya existen en la aplicación.

---

## Regla crítica antes de modificar código

Cuando una solicitud implique cambios importantes en:

- arquitectura;
- base de datos;
- dashboard;
- cálculo de auditorías;
- generación de PDF;
- modelo de categorías/subcategorías/cuestionarios;

PRIMERO inspeccionar el código existente.

Antes de implementar, entregar un diagnóstico breve indicando:

1. Qué existe actualmente.
2. Qué puede reutilizarse.
3. Qué necesita modificarse.
4. Qué realmente hace falta crear.
5. Riesgos de afectar funcionalidades existentes.
6. Plan de implementación propuesto.

NO modificar código hasta recibir autorización explícita cuando la solicitud indique que estamos en fase de diagnóstico.

---

## Regla general

No hacer refactorizaciones masivas innecesarias.

No cambiar tecnologías o instalar nuevas dependencias si la solución actual puede extenderse adecuadamente.

Antes de reemplazar una solución existente, justificar técnicamente por qué.

El objetivo es evolucionar la aplicación sin romper funcionalidades existentes.