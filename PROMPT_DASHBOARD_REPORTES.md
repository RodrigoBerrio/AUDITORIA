PROMPT_DASHBOARD_REPORTES.md

# PROMPT_DASHBOARD_REPORTES.md

# Dashboard e Informes de Auditoría Empresarial

## 1. Objetivo

Analiza y posteriormente prepara la aplicación para generar:

1. Dashboards ejecutivos de auditoría.
2. Informes ejecutivos en PDF.
3. Informes independientes por subcategoría.
4. Informes independientes por categoría.
5. Informes consolidados de varias categorías.
6. Informe integral cuando se evalúen todas las categorías.
7. Informes progresivos durante el desarrollo de la auditoría.

IMPORTANTE:
## I. Reutilización y cambios sobre código existente

Clasifica los componentes, servicios, funciones, utilidades y demás código relacionado con dashboards e informes como:

- **REUTILIZAR**
- **EXTENDER**
- **MODIFICAR**
- **CREAR**
- **ELIMINAR**

Para cada elemento relevante indica brevemente:

| Elemento | Acción propuesta | Motivo | Riesgo |
|---|---|---|---|
| Componente/archivo | REUTILIZAR / EXTENDER / MODIFICAR / CREAR / ELIMINAR | Razón | Bajo/Medio/Alto |

Especialmente para **ELIMINAR**:

- verifica primero que el código no sea utilizado por otra funcionalidad;
- identifica sus dependencias;
- comprueba si fue reemplazado por otra implementación;
- explica por qué ya no es necesario;
- evalúa el impacto sobre dashboards, PDF, cálculos y datos históricos.

**No elimines ningún archivo, componente, función, dependencia o código durante el diagnóstico.**

Toda eliminación debe ser propuesta primero y requerirá mi aprobación explícita antes de ejecutarse.

La aplicación YA contiene código para dashboards, gráficos, cálculos e informes PDF.

NO desarrolles estas funcionalidades desde cero sin inspeccionar primero lo existente.

Prioridad:

**Reutilizar → Extender → Modificar → Crear → Eliminar**

**Eliminar** solo podrá hacerse después de identificar dependencias, explicar el motivo y recibir mi aprobación explícita.

---

# 2. Documentos de referencia

Revisa los documentos disponibles en `docs/`.

Entre ellos estarán:

- `STARBUCKS_Reporte_Julio_2026.pdf`
- `EsquemaDeHistogramaPorcategorias.png`
- cuestionarios de la categoría Mantenimiento.

## STARBUCKS_Reporte_Julio_2026

Utilízalo únicamente como referencia conceptual y ejecutiva para:

- resumen ejecutivo;
- KPIs;
- semáforos;
- hallazgos principales;
- recomendaciones;
- priorización;
- plan de acción;
- organización visual del informe.

NO copies:

- datos;
- cifras;
- marca;
- textos;
- contenido específico;
- colores exactos.

La filosofía que quiero aprovechar es:

**Resultado → Hallazgo → Criticidad → Recomendación → Acción**

---

# 3. Cuestionarios existentes

Los cuestionarios adjuntos YA existen total o parcialmente dentro de la aplicación.

NO los vuelvas a crear ni importar automáticamente.

Úsalos para validar:

- estructura;
- categorías;
- subcategorías;
- dimensiones/bloques;
- preguntas;
- evidencias;
- observaciones;
- escala de evaluación.

Si encuentras diferencias entre los documentos y la aplicación:

NO las corrijas automáticamente.

Primero presenta:

| Elemento | Documento | Aplicación | Diferencia | Recomendación |
|---|---|---|---|---|

y espera autorización.

---

# 4. Categoría inicial: Mantenimiento

Utiliza Mantenimiento como primera implementación y validación del nuevo enfoque.

Actualmente existen cuestionarios relacionados con:

- Almacén de Repuestos
- Cultura de Mantenimiento
- Estrategia de Mantenimiento
- Habilidades del Personal de Mantenimiento
- Mediciones de Mantenimiento
- Órdenes de Trabajo

Esta lista NO debe quedar hardcodeada si la aplicación ya dispone de una estructura dinámica.

---

# 5. Estructura de evaluación

Los cuestionarios contienen bloques o dimensiones internas.

Ejemplos:

## Almacén de Repuestos

- Almacenamiento de repuestos
- Gestión de inventario
- Control y trazabilidad
- Compras y proveedores
- Obsolescencia y provisiones

## Cultura de Mantenimiento

- Enfoque organizacional hacia mantenimiento
- Comportamientos y prácticas del personal
- Comunicación, aprendizaje y mejora continua

## Estrategia de Mantenimiento

Incluye elementos como:

- Estrategia del departamento
- Green Rooms / Cuartos Verdes
- Master Plan
- Árbol de pérdidas, costos y oportunidades
- Assessment de mantenimiento

## Habilidades

- Identificación y evaluación de competencias
- Formación, capacitación y desarrollo
- Aplicación, seguimiento y mejora

## Mediciones

- Medición del desempeño
- Medición técnica
- Análisis, control y mejora continua

## Órdenes de Trabajo

- Generación y control
- Planificación y asignación
- Ejecución y registro
- Cierre, trazabilidad y análisis

Antes de crear nuevas entidades o tablas, verifica cómo representa actualmente la aplicación estos niveles.

Jerarquía conceptual:

**Auditoría**
→ **Categoría**
→ **Subcategoría**
→ **Cuestionario**
→ **Dimensión/Bloque**
→ **Pregunta**
→ **Respuesta**

No agregues niveles innecesarios si el modelo actual ya puede representar esta estructura.

---

# 6. Escala de evaluación

La auditoría utiliza principalmente una escala:

**1 a 5**

Mantener la metodología actualmente implementada.

Como referencia visual:

- 🔴 Crítico: < 2
- 🟠 Serio: 2 a < 3
- 🟡 Alerta: 3 a < 4
- 🟢 Bueno: ≥ 4

Los rangos deben estar centralizados y ser configurables.

NO cambies rangos o fórmulas existentes sin explicar primero el impacto y obtener aprobación.

---

# 7. Dashboard de Mantenimiento

Utiliza `EsquemaDeHistogramaPorcategorias.png` como referencia principal.

La visión ejecutiva debe mostrar inicialmente:

- puntaje global de Mantenimiento;
- subcategorías evaluadas;
- subcategorías críticas;
- hallazgos prioritarios.

Después mostrar una cuadrícula de resultados.

Cada tarjeta debe representar una subcategoría evaluada.

Ejemplo:

### Almacén de Repuestos — 3.4 / 5

Almacenamiento        ███████░░░ 3.6  
Inventario            █████░░░░░ 2.5  
Trazabilidad          ████████░░ 4.1  
Compras               ██████░░░░ 3.0  
Obsolescencia         ████░░░░░░ 2.1

Escala:

**0–5**

Los nombres deben provenir de los datos reales.

NO inventar indicadores si existen dimensiones definidas.

---

# 8. Drill-down

El dashboard debe permitir navegar:

**Auditoría**

→ **Categoría**

→ **Subcategoría**

→ **Dimensión**

→ **Preguntas**

→ **Respuestas**

→ **Evidencias**

→ **Observaciones**

Todo resultado debe ser trazable hasta sus respuestas originales.

---

# 9. Vista tabular

Mantener o incorporar, según lo que ya exista:

**Ver como tabla**

Ejemplo:

| Subcategoría | Dimensión | Puntaje | Estado |
|---|---|---:|---|

Tabla y gráficos deben consumir la misma lógica de resultados.

NO duplicar cálculos.

---

# 10. Informes independientes

La aplicación NO debe asumir que siempre se auditan todas las categorías.

Puede evaluarse:

- una subcategoría;
- varias subcategorías;
- una categoría;
- varias categorías;
- todas las categorías;
- cuestionarios específicos.

Debe ser posible generar:

### Informe de Subcategoría

Ejemplo:

**Informe — Órdenes de Trabajo**

### Informe de Categoría

Ejemplo:

**Informe — Mantenimiento**

### Informe Consolidado

Ejemplo:

**Mantenimiento + Energía**

### Informe Integral

Cuando se evalúe el alcance completo definido para la auditoría.

Principio fundamental:

> **El alcance determina el informe.**

---

# 11. INFORMES PROGRESIVOS

Este requisito es CRÍTICO.

La aplicación NO debe obligar al usuario a finalizar toda la auditoría para poder consultar resultados o generar informes.

Los resultados e informes deben poder construirse **progresivamente a medida que avanza la auditoría**.

Ejemplo:

Si dentro de Mantenimiento solamente se ha completado:

**Almacén de Repuestos**

debe ser posible obtener:

**Informe preliminar — Almacén de Repuestos**

Cuando posteriormente se complete:

**Órdenes de Trabajo**

debe poder obtenerse:

- Informe de Almacén de Repuestos
- Informe de Órdenes de Trabajo
- Informe preliminar consolidado de Mantenimiento con ambas subcategorías

Si posteriormente se completan las demás subcategorías de Mantenimiento:

el mismo informe de Mantenimiento debe evolucionar hasta convertirse en:

**Informe final — Mantenimiento**

sin necesidad de crear otro mecanismo de reporte.

---

# 12. Actualización progresiva

A medida que se respondan nuevas preguntas y se completen:

- dimensiones;
- cuestionarios;
- subcategorías;
- categorías;

el dashboard y los informes deben actualizar sus resultados.

Conceptualmente:

```text
Preguntas respondidas
        ↓
Resultado de dimensión
        ↓
Resultado de subcategoría
        ↓
Resultado de categoría
        ↓
Resultado consolidado
```

El sistema debe reutilizar siempre la misma lógica de cálculo.

---

# 13. Informe preliminar vs informe final

Debe existir una diferenciación clara entre:

### INFORME PRELIMINAR

Cuando el alcance seleccionado todavía no está completamente evaluado.

### INFORME FINAL

Cuando el alcance correspondiente está completamente evaluado/cerrado.

El informe preliminar debe indicar claramente:

- qué fue evaluado;
- qué está pendiente;
- fecha del corte;
- porcentaje o estado de avance, si la aplicación dispone de información suficiente.

NO presentar resultados pendientes como cero.

---

# 14. Progresividad entre categorías

La misma lógica debe funcionar cuando se agreguen categorías.

Ejemplo:

Primero se completa:

**Mantenimiento**

Resultado:

**Informe Mantenimiento**

Después se completa:

**Energía**

Resultado:

- Informe Mantenimiento
- Informe Energía
- Informe Consolidado Mantenimiento + Energía

Después se completa:

**Sostenibilidad**

Resultado:

- informes individuales;
- consolidado actualizado:

**Mantenimiento + Energía + Sostenibilidad**

Cuando se complete todo el alcance:

**Informe Integral de Auditoría Empresarial**

---

# 15. No evaluado no significa cero

Regla crítica:

**NO EVALUADO ≠ 0**

Una categoría, subcategoría, dimensión o pregunta pendiente o fuera del alcance NO debe reducir artificialmente los resultados.

Diferenciar correctamente:

- evaluado;
- pendiente;
- no evaluado;
- no aplica.

---

# 16. Estados de evaluación

Analiza si actualmente existe una forma de diferenciar:

- No iniciada
- En progreso
- Completada
- No aplica / fuera del alcance

NO crees nuevos estados automáticamente.

Primero determina qué existe actualmente y recomienda cambios solo si son necesarios.

---

# 17. Dashboard consolidado

Cuando existan varias categorías evaluadas, debe existir una vista consolidada.

Ejemplo:

| Categoría | Puntaje | Estado |
|---|---:|---|
| Mantenimiento | 3.2 | 🟡 Alerta |
| Energía | 2.7 | 🟠 Serio |
| Sostenibilidad | 4.1 | 🟢 Bueno |

Mostrar:

- categorías evaluadas;
- resultado consolidado, si metodológicamente corresponde;
- categorías críticas;
- hallazgos prioritarios;
- acciones prioritarias.

El dashboard debe funcionar dinámicamente con cualquier número de categorías.

---

# 18. Informes PDF

La aplicación YA posee código para generación de PDF.

Antes de elegir otra solución:

1. Localiza la implementación actual.
2. Identifica la librería utilizada.
3. Identifica cómo obtiene los datos.
4. Determina qué puede reutilizarse.
5. Identifica sus limitaciones.

NO instales otra librería PDF si la solución actual puede extenderse adecuadamente.

---

# 19. Informe PDF de Categoría

Para Mantenimiento buscar un informe ejecutivo aproximadamente de:

**6–10 páginas**, dependiendo de los resultados.

NO utilizar una página por pregunta.

Estructura sugerida:

## Portada

- Empresa
- Categoría
- Fecha
- Auditoría
- Responsable, si existe
- Puntaje global
- Estado: preliminar/final

## Resumen Ejecutivo

- puntaje global;
- avance de evaluación, cuando corresponda;
- subcategorías evaluadas;
- subcategorías críticas;
- hallazgos prioritarios.

## Resultados

Representación basada en:

`EsquemaDeHistogramaPorcategorias.png`

## Fortalezas

Máximo 3–5.

## Aspectos prioritarios

Máximo 3–5.

## Hallazgos y recomendaciones

Solo los relevantes.

## Plan de Acción

Tabla compacta y priorizada.

---

# 20. Informe de Subcategoría

Una subcategoría debe poder generar su propio informe.

Mostrar:

- puntaje general;
- dimensiones evaluadas;
- resultados;
- fortalezas;
- debilidades;
- preguntas críticas cuando sean relevantes;
- evidencias importantes;
- observaciones relevantes;
- hallazgos;
- recomendaciones;
- acciones.

NO volver a imprimir innecesariamente todo el cuestionario.

---

# 21. Informe Consolidado PDF

Cuando existan dos o más categorías evaluadas permitir:

# Informe Consolidado de Auditoría Empresarial

NO debe ser simplemente la unión de PDFs individuales.

Debe tener lógica ejecutiva propia.

Estructura:

## Portada

- Empresa
- Auditoría
- Fecha
- Alcance
- Categorías evaluadas
- Estado preliminar/final

## Resumen Ejecutivo

- resultado consolidado;
- avance;
- categorías evaluadas;
- categorías críticas;
- hallazgos prioritarios;
- acciones prioritarias.

## Comparativo por Categoría

Ejemplo:

Mantenimiento    ██████░░░░ 3.2  
Energía          █████░░░░░ 2.7  
Sostenibilidad   ████████░░ 4.1

## Principales hallazgos globales

Máximo aproximado:

**5–10**

## Plan de acción consolidado

| Prioridad | Acción | Categoría | Alcance | Horizonte | Impacto |
|---|---|---|---|---|---|

---

# 22. Hallazgos

NO convertir automáticamente cada respuesta baja en un hallazgo independiente.

Agrupar problemas relacionados cuando sea técnicamente razonable.

Ejemplo:

Varias respuestas deficientes relacionadas con planificación de OT pueden generar:

**Hallazgo:**

> Debilidades en la planificación y preparación de las órdenes de trabajo.

Cada hallazgo debe poder relacionarse con:

- categoría;
- subcategoría;
- dimensión;
- preguntas origen;
- criticidad;
- evidencia;
- recomendación.

---

# 23. Hallazgos principales

En las vistas ejecutivas mostrar aproximadamente:

**Top 5 hallazgos**

por alcance.

Cada uno debe indicar:

- categoría/subcategoría;
- dimensión;
- puntaje;
- criticidad;
- descripción;
- recomendación.

---

# 24. Recomendaciones

Las recomendaciones deben ser:

- concretas;
- ejecutables;
- breves;
- relacionadas con el hallazgo;
- sustentadas por los resultados.

Evitar recomendaciones genéricas.

---

# 25. Plan de acción

Cada informe debe generar un plan correspondiente a su alcance.

Ejemplo:

| Prioridad | Acción | Alcance | Horizonte | Impacto |
|---|---|---|---|---|

Cuando existan varias categorías:

# Plan de Acción Consolidado

Los hallazgos de diferentes categorías deben competir por prioridad.

No agrupar simplemente por categoría.

---

# 26. Impacto económico

Cuando existan datos suficientes, mostrar:

- costo actual;
- ahorro potencial;
- reducción;
- retorno estimado.

NUNCA inventar cifras.

Cuando no sea cuantificable:

**Por cuantificar**

---

# 27. Fuente única de resultados

NO quiero:

- cálculo A para dashboard;
- cálculo B para PDF;
- cálculo C para consolidado.

Debe existir una única lógica reutilizable para:

- dimensión;
- subcategoría;
- categoría;
- consolidado;
- criticidad;
- hallazgos;
- prioridades.

Dashboard y PDF deben consumir los mismos resultados.

---

# 28. Cálculos

Antes de modificar fórmulas, REVISA cómo calcula actualmente la aplicación.

No modificar silenciosamente:

- promedios;
- ponderaciones;
- tratamiento de No Aplica;
- preguntas pendientes;
- auditorías parciales;
- rangos de madurez.

Si no existen ponderaciones, NO agregarlas automáticamente.

---

# 29. Escalabilidad

En el futuro se agregarán nuevas:

- categorías;
- subcategorías;
- dimensiones;
- cuestionarios.

El motor debe permitir incorporarlas sin construir desde cero:

- dashboard;
- PDF;
- cálculos;
- consolidado.

Debe funcionar con:

**1 categoría → varias categorías → todas las categorías.**

---

# 30. Indicadores especializados

Una categoría puede necesitar indicadores específicos.

Ejemplo:

Mantenimiento:

- MTBF;
- MTTR;
- disponibilidad;
- cumplimiento preventivo.

Energía:

- kWh;
- demanda;
- factor de potencia;
- costo energético.

Estos indicadores pueden utilizar componentes especializados sin romper el motor general.

Primero identifica qué indicadores ya existen.

---

# 31. Responsive y diseño

El dashboard debe funcionar en:

- escritorio;
- tablet;
- celular.

Mantener apariencia:

- profesional;
- empresarial;
- limpia;
- ejecutiva.

Priorizar:

- tarjetas KPI;
- barras horizontales;
- semáforos;
- tablas compactas;
- jerarquía visual.

Evitar:

- gráficos 3D;
- exceso de gráficos;
- exceso de colores;
- textos extensos;
- duplicación de información.

Cada elemento debe responder:

**¿Cómo estamos?**

**¿Dónde está el problema?**

**¿Qué tan grave es?**

**¿Qué debemos hacer?**

---

# 32. Integridad y compatibilidad

No romper:

- auditorías existentes;
- cuestionarios existentes;
- respuestas almacenadas;
- navegación;
- autenticación;
- base de datos/Supabase;
- dashboards existentes;
- PDFs existentes.

Si una modificación pudiera afectar datos históricos:

DETENTE y explica el riesgo antes de implementarla.

---

# 33. PRIMERA FASE OBLIGATORIA: SOLO DIAGNÓSTICO

## NO IMPLEMENTES NADA TODAVÍA.

Inspecciona primero el proyecto.

Entrégame únicamente:

## A. Dashboard actual

- componentes;
- gráficos;
- fuentes de datos;
- cálculos;
- elementos reutilizables.

## B. PDF actual

- librería;
- flujo;
- ubicación;
- datos utilizados;
- limitaciones;
- elementos reutilizables.

## C. Modelo de datos

Explica cómo están representados:

**Auditoría**
→ **Categoría**
→ **Subcategoría**
→ **Cuestionario**
→ **Dimensión**
→ **Pregunta**
→ **Respuesta**

Indica qué niveles existen realmente.

## D. Alcance de auditoría

Determina:

1. ¿Puede auditarse solo una categoría?
2. ¿Pueden auditarse varias?
3. ¿Existe un concepto de alcance?
4. ¿Cómo se representa "No evaluado"?
5. ¿Existe dependencia que obligue a completar toda la auditoría?

## E. Progresividad

Analiza específicamente:

1. ¿Puede obtenerse actualmente un resultado con una auditoría parcialmente diligenciada?
2. ¿Puede generarse un informe de una subcategoría completada sin terminar la categoría?
3. ¿Puede actualizarse el consolidado cuando se completa una nueva subcategoría?
4. ¿Puede actualizarse el consolidado cuando se agrega una nueva categoría evaluada?
5. ¿Cómo distinguirías informe preliminar de informe final?
6. ¿Qué cambios mínimos serían necesarios para soportar informes progresivos?

## F. Resultados

Explica cómo se calculan actualmente:

- dimensión;
- subcategoría;
- categoría;
- auditoría completa.

## G. Informes independientes

Determina si actualmente puede generarse:

- informe de subcategoría;
- informe de categoría;
- informe multcategoría;
- informe integral;
- informe progresivo/preliminar.

## H. Documentos vs aplicación

Compara los cuestionarios suministrados con la estructura existente.

NO modifiques nada.

## I. Reutilización

Clasifica los cambios como:

- REUTILIZAR
- EXTENDER
- MODIFICAR
- CREAR

## J. Riesgos

Identifica posibles impactos sobre:

- datos históricos;
- base de datos;
- cálculos;
- cuestionarios;
- dashboard;
- PDF;
- navegación.

## K. Plan

Propón un plan de implementación de:

**máximo 5 etapas**

pequeñas y verificables.

---

# 34. DETENTE DESPUÉS DEL DIAGNÓSTICO

Después de entregar los puntos A–K:

**DETENTE.**

NO:

- escribas código;
- modifiques archivos;
- ejecutes migraciones;
- instales dependencias;
- cambies tablas;
- cambies fórmulas;
- refactorices componentes.

Espera mi aprobación.

Después implementaremos:

**una etapa a la vez.**

---

# PRINCIPIO FINAL

El sistema debe evolucionar progresivamente junto con la auditoría:

```text
Respuestas
    ↓
Informe de Subcategoría
    ↓
Informe de Categoría
    ↓
Informe Consolidado
    ↓
Informe Integral
```

Sin esperar necesariamente a completar toda la auditoría.

Los informes parciales deben identificarse como:

**PRELIMINARES**

y evolucionar automáticamente conforme se complete el alcance.

El objetivo final es:

**Datos**
→ **Resultados**
→ **Dashboard**
→ **Hallazgos**
→ **Recomendaciones**
→ **Plan de acción**
→ **PDF ejecutivo**

manteniendo siempre:

**trazabilidad + modularidad + progresividad + reutilización del código existente.**