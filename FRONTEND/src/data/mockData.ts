// ============================================================
// Datos de ejemplo — con la MISMA forma que tendrá la futura
// API sobre el esquema Postgres v3. Mientras no exista el
// backend real, este módulo hace de "fuente de verdad" única;
// cuando el backend exista, solo se reemplaza este archivo por
// llamadas fetch/axios con la misma forma de datos.
// ============================================================
import type {
  Empresa, Categoria, Subcategoria, Cuestionario, Pregunta,
  Auditoria, Reporte, Hallazgo, Usuario,
} from '../types/domain';

export const usuarioActual: Usuario = {
  id: 'u-1',
  nombre: 'Oscar Alberto Berrío R.',
  correo: 'oscar.berrio@auditoriasindustriales.com',
  rol: 'auditor',
  activo: true,
};

export const empresas: Empresa[] = [];

// Catálogo real de categorías/subcategorías/cuestionarios/preguntas.
// Se llena a medida que se definen los cuestionarios definitivos de cada área.
export const categorias: Categoria[] = [
  { id: 'cat-mant', nombre: 'Mantenimiento', icono: 'ti-settings', esPlantilla: true, activo: true },
];

export const subcategorias: Subcategoria[] = [
  { id: 'sub-mant-estrategia', categoriaId: 'cat-mant', nombre: 'Estrategia de mantenimiento', esPlantilla: true, activo: true },
  { id: 'sub-mant-ot', categoriaId: 'cat-mant', nombre: 'Órdenes de trabajo', esPlantilla: true, activo: true },
  { id: 'sub-mant-almacen', categoriaId: 'cat-mant', nombre: 'Almacén de repuestos', esPlantilla: true, activo: true },
  { id: 'sub-mant-cultura', categoriaId: 'cat-mant', nombre: 'Cultura de mantenimiento', esPlantilla: true, activo: true },
  { id: 'sub-mant-info', categoriaId: 'cat-mant', nombre: 'Gestión de la información', esPlantilla: true, activo: true },
  { id: 'sub-mant-habilidades', categoriaId: 'cat-mant', nombre: 'Habilidades del personal de mantenimiento', esPlantilla: true, activo: true },
  { id: 'sub-mant-mediciones', categoriaId: 'cat-mant', nombre: 'Mediciones de mantenimiento', esPlantilla: true, activo: true },
];

export const cuestionarios: Cuestionario[] = [
  { id: 'q-estrategia', subcategoriaId: 'sub-mant-estrategia', nombre: 'Checklist de estrategia de mantenimiento', numPreguntas: 0, activo: true },
  { id: 'q-ot', subcategoriaId: 'sub-mant-ot', nombre: 'Checklist de órdenes de trabajo', numPreguntas: 0, activo: true },
  { id: 'q-almacen-1', subcategoriaId: 'sub-mant-almacen', nombre: 'Almacenamiento y control de repuestos', numPreguntas: 0, activo: true },
  { id: 'q-almacen-2', subcategoriaId: 'sub-mant-almacen', nombre: 'Obsolescencia y provisiones', numPreguntas: 0, activo: true },
  { id: 'q-cultura', subcategoriaId: 'sub-mant-cultura', nombre: 'Checklist de cultura de mantenimiento', numPreguntas: 0, activo: true },
  { id: 'q-info', subcategoriaId: 'sub-mant-info', nombre: 'Checklist de gestión de la información', numPreguntas: 0, activo: true },
  { id: 'q-habilidades', subcategoriaId: 'sub-mant-habilidades', nombre: 'Checklist de habilidades del personal', numPreguntas: 0, activo: true },
  { id: 'q-mediciones', subcategoriaId: 'sub-mant-mediciones', nombre: 'Checklist de mediciones de mantenimiento', numPreguntas: 0, activo: true },
];

// --- Preguntas por cuestionario. numero conserva el ID de la tabla del documento original ---
// (usado solo como referencia de origen; el orden mostrado al auditor es siempre posicional).

const preguntasEstrategia: Pregunta[] = [
  { id: 'p-estr-1', cuestionarioId: 'q-estrategia', numero: 1, texto: '¿Existe una estrategia claramente definida y documentada del departamento de mantenimiento?', evidencia: 'Documento de estrategia', activo: true },
  { id: 'p-estr-2', cuestionarioId: 'q-estrategia', numero: 2, texto: '¿La estrategia ha sido comunicada y es entendida por todo el personal de mantenimiento?', evidencia: 'Entrevistas / comunicación interna', activo: true },
  { id: 'p-estr-3', cuestionarioId: 'q-estrategia', numero: 3, texto: '¿La estrategia cuenta con criterios medibles y cuantificables (SQCDM)?', evidencia: 'Indicadores / documentos', activo: true },
  { id: 'p-estr-4', cuestionarioId: 'q-estrategia', numero: 4, texto: '¿Los criterios de la estrategia son monitoreados, exhibidos y con responsabilidades desplegadas?', evidencia: 'Tableros / seguimiento / roles', activo: true },
  { id: 'p-estr-5', cuestionarioId: 'q-estrategia', numero: 5, texto: '¿Se evidencia el cumplimiento de la estrategia mediante indicadores y resultados efectivos?', evidencia: 'KPIs / reportes', activo: true },
  { id: 'p-estr-6', cuestionarioId: 'q-estrategia', numero: 6, texto: '¿Existen cuartos verdes implementados en la organización?', evidencia: 'Observación en planta', activo: true },
  { id: 'p-estr-7', cuestionarioId: 'q-estrategia', numero: 7, texto: '¿El concepto de cuarto verde está desarrollado y correctamente implementado?', evidencia: 'Observación / entrevistas', activo: true },
  { id: 'p-estr-8', cuestionarioId: 'q-estrategia', numero: 8, texto: '¿El cuarto verde es usado activamente por el equipo y se encuentra en proceso de maduración?', evidencia: 'Reuniones / uso del espacio', activo: true },
  { id: 'p-estr-9', cuestionarioId: 'q-estrategia', numero: 9, texto: '¿Los tableros del cuarto verde son interactivos y permiten gestionar metas desde la gerencia?', evidencia: 'Tableros / indicadores', activo: true },
  { id: 'p-estr-10', cuestionarioId: 'q-estrategia', numero: 10, texto: '¿El cuarto verde se utiliza como espacio de aprendizaje (LUP / OPL) y evidencia mejora de indicadores?', evidencia: 'Registros / evidencias / indicadores', activo: true },
  { id: 'p-estr-11', cuestionarioId: 'q-estrategia', numero: 11, texto: '¿Existe un Master Plan del departamento de mantenimiento?', evidencia: 'Documento Master Plan', activo: true },
  { id: 'p-estr-12', cuestionarioId: 'q-estrategia', numero: 12, texto: '¿El Master Plan está alineado con la estrategia del mantenimiento?', evidencia: 'Plan / estrategia', activo: true },
  { id: 'p-estr-13', cuestionarioId: 'q-estrategia', numero: 13, texto: '¿El Master Plan ha sido divulgado, exhibido y comprendido por el personal?', evidencia: 'Comunicación / entrevistas', activo: true },
  { id: 'p-estr-14', cuestionarioId: 'q-estrategia', numero: 14, texto: '¿El Master Plan es monitoreado frecuentemente y genera acciones?', evidencia: 'Seguimiento / reuniones', activo: true },
  { id: 'p-estr-15', cuestionarioId: 'q-estrategia', numero: 15, texto: '¿El Master Plan se ejecuta y los resultados (KPIs) son evidentes?', evidencia: 'Indicadores / reportes', activo: true },
  { id: 'p-estr-16', cuestionarioId: 'q-estrategia', numero: 16, texto: '¿Existe un Árbol de Pérdidas, Costos y Oportunidades?', evidencia: 'Documento / análisis', activo: true },
  { id: 'p-estr-17', cuestionarioId: 'q-estrategia', numero: 17, texto: '¿El personal comprende el concepto del Árbol de Pérdidas?', evidencia: 'Entrevistas', activo: true },
  { id: 'p-estr-18', cuestionarioId: 'q-estrategia', numero: 18, texto: '¿El personal entiende la relación del árbol con objetivos, actividades, programas y KPIs?', evidencia: 'Entrevistas / análisis', activo: true },
  { id: 'p-estr-19', cuestionarioId: 'q-estrategia', numero: 19, texto: '¿El Árbol de Pérdidas es utilizado como parte integral de la estrategia de mantenimiento?', evidencia: 'Planes / gestión', activo: true },
  { id: 'p-estr-20', cuestionarioId: 'q-estrategia', numero: 20, texto: '¿Los líderes revisan regularmente actividades de mejora enfocada relacionadas con el árbol?', evidencia: 'Actas / seguimiento', activo: true },
  { id: 'p-estr-21', cuestionarioId: 'q-estrategia', numero: 21, texto: '¿Existe un proceso de assessment de mantenimiento?', evidencia: 'Procedimiento', activo: true },
  { id: 'p-estr-22', cuestionarioId: 'q-estrategia', numero: 22, texto: '¿El assessment tiene programación definida y seguimiento de acciones?', evidencia: 'Cronograma / seguimiento', activo: true },
  { id: 'p-estr-23', cuestionarioId: 'q-estrategia', numero: 23, texto: '¿El assessment se realiza regularmente con acciones bien definidas?', evidencia: 'Reportes / planes de acción', activo: true },
  { id: 'p-estr-24', cuestionarioId: 'q-estrategia', numero: 24, texto: '¿Los líderes tienen responsabilidad formal sobre los planes de acción del assessment?', evidencia: 'Asignación / evaluación', activo: true },
  { id: 'p-estr-25', cuestionarioId: 'q-estrategia', numero: 25, texto: '¿Existe un proceso formal de benchmarking externo para mejorar el modelo?', evidencia: 'Documentos / estudios', activo: true },
  { id: 'p-estr-26', cuestionarioId: 'q-estrategia', numero: 26, texto: '¿Los objetivos del assessment están definidos claramente?', evidencia: 'Documento / indicadores', activo: true },
  { id: 'p-estr-27', cuestionarioId: 'q-estrategia', numero: 27, texto: '¿Los resultados del assessment se utilizan para medir el desempeño del área?', evidencia: 'Reportes / KPIs', activo: true },
  { id: 'p-estr-28', cuestionarioId: 'q-estrategia', numero: 28, texto: '¿El equipo participa en el análisis de resultados y planes de acción?', evidencia: 'Reuniones / actas', activo: true },
  { id: 'p-estr-29', cuestionarioId: 'q-estrategia', numero: 29, texto: '¿Los resultados y planes se exhiben en el cuarto verde?', evidencia: 'Tableros / evidencias', activo: true },
  { id: 'p-estr-30', cuestionarioId: 'q-estrategia', numero: 30, texto: '¿Los KPIs muestran tendencia positiva alineada con los objetivos?', evidencia: 'Indicadores', activo: true },
  { id: 'p-estr-31', cuestionarioId: 'q-estrategia', numero: 31, texto: '¿El área de mantenimiento supera el desempeño esperado y actúa como referente?', evidencia: 'Benchmark / resultados', activo: true },
];

const preguntasOT: Pregunta[] = [
  { id: 'p-ot-1', cuestionarioId: 'q-ot', numero: 1, texto: '¿La empresa cuenta con un formato estandarizado para la generación de órdenes de trabajo?', evidencia: 'Formato físico o digital de OT / CMMS / ERP', activo: true },
  { id: 'p-ot-2', cuestionarioId: 'q-ot', numero: 2, texto: '¿Cada orden de trabajo se genera de manera formal y no solo verbalmente?', evidencia: 'Procedimiento interno / Registro de solicitudes / Sistema de mantenimiento', activo: true },
  { id: 'p-ot-3', cuestionarioId: 'q-ot', numero: 3, texto: '¿Cada orden de trabajo tiene un código o consecutivo único para su trazabilidad?', evidencia: 'Numeración consecutiva / Sistema OT / Base de datos', activo: true },
  { id: 'p-ot-4', cuestionarioId: 'q-ot', numero: 4, texto: '¿Las órdenes de trabajo registran fecha de creación y fecha programada de ejecución?', evidencia: 'Orden de trabajo diligenciada / Software CMMS', activo: true },
  { id: 'p-ot-5', cuestionarioId: 'q-ot', numero: 5, texto: '¿La orden de trabajo identifica claramente el equipo, activo o sistema a intervenir?', evidencia: 'Campo de activo / Código de equipo / Hoja de vida del equipo', activo: true },
  { id: 'p-ot-6', cuestionarioId: 'q-ot', numero: 6, texto: '¿Las órdenes clasifican el tipo de intervención (preventiva, correctiva, predictiva, mejora, inspección)?', evidencia: 'Formato OT / Catálogo de tipos de mantenimiento / Sistema', activo: true },
  { id: 'p-ot-7', cuestionarioId: 'q-ot', numero: 7, texto: '¿Las órdenes de trabajo contemplan prioridad o nivel de criticidad?', evidencia: 'Campo de prioridad / Matriz de criticidad / CMMS', activo: true },
  { id: 'p-ot-8', cuestionarioId: 'q-ot', numero: 8, texto: '¿Las órdenes de trabajo son revisadas y planificadas antes de su ejecución?', evidencia: 'Plan semanal / Programación / Backlog de mantenimiento', activo: true },
  { id: 'p-ot-9', cuestionarioId: 'q-ot', numero: 9, texto: '¿Se asigna un técnico o responsable específico para ejecutar cada orden?', evidencia: 'Campo responsable / Programación de personal / Sistema OT', activo: true },
  { id: 'p-ot-10', cuestionarioId: 'q-ot', numero: 10, texto: '¿La orden de trabajo incluye una descripción clara de la labor a realizar?', evidencia: 'Orden diligenciada / Instrucción técnica / Aviso de mantenimiento', activo: true },
  { id: 'p-ot-11', cuestionarioId: 'q-ot', numero: 11, texto: '¿Se estiman previamente tiempos de intervención para las órdenes programadas?', evidencia: 'Tiempo estándar / Programación / Históricos de mantenimiento', activo: true },
  { id: 'p-ot-12', cuestionarioId: 'q-ot', numero: 12, texto: '¿La empresa identifica previamente los repuestos, materiales o herramientas requeridas?', evidencia: 'Lista de materiales / Reserva de repuestos / Planeación OT', activo: true },
  { id: 'p-ot-13', cuestionarioId: 'q-ot', numero: 13, texto: '¿Las órdenes de trabajo se priorizan en función del impacto operativo o riesgo del activo?', evidencia: 'Matriz de prioridad / Criticidad / Procedimiento de planificación', activo: true },
  { id: 'p-ot-14', cuestionarioId: 'q-ot', numero: 14, texto: '¿Existe coordinación entre mantenimiento y operación para liberar el equipo antes de ejecutar la orden?', evidencia: 'Permisos de trabajo / Registros de liberación / Coordinación con producción', activo: true },
  { id: 'p-ot-15', cuestionarioId: 'q-ot', numero: 15, texto: '¿Las órdenes registran la fecha y hora real de inicio de la intervención?', evidencia: 'OT cerrada / Sistema CMMS / Parte de trabajo', activo: true },
  { id: 'p-ot-16', cuestionarioId: 'q-ot', numero: 16, texto: '¿Las órdenes registran la fecha y hora real de finalización?', evidencia: 'Orden cerrada / Registro técnico / Sistema', activo: true },
  { id: 'p-ot-17', cuestionarioId: 'q-ot', numero: 17, texto: '¿Se documentan detalladamente las actividades ejecutadas durante la intervención?', evidencia: 'Campo de actividades realizadas / Informe técnico / OT', activo: true },
  { id: 'p-ot-18', cuestionarioId: 'q-ot', numero: 18, texto: '¿Se registran las fallas encontradas o condiciones observadas durante el trabajo?', evidencia: 'Descripción de falla / Diagnóstico / Reporte técnico', activo: true },
  { id: 'p-ot-19', cuestionarioId: 'q-ot', numero: 19, texto: '¿Se documentan las acciones correctivas o soluciones aplicadas?', evidencia: 'Campo solución / Cierre técnico / Informe de mantenimiento', activo: true },
  { id: 'p-ot-20', cuestionarioId: 'q-ot', numero: 20, texto: '¿Se registran los repuestos o materiales efectivamente utilizados en la orden?', evidencia: 'Vale de almacén / OT / Consumo de repuestos / Sistema', activo: true },
  { id: 'p-ot-21', cuestionarioId: 'q-ot', numero: 21, texto: '¿El técnico ejecutor deja validación, nombre, firma o registro de responsabilidad de la intervención?', evidencia: 'Firma física / Usuario del sistema / Validación electrónica', activo: true },
  { id: 'p-ot-22', cuestionarioId: 'q-ot', numero: 22, texto: '¿Las órdenes de trabajo son cerradas formalmente una vez ejecutadas?', evidencia: 'Estado cerrada / Procedimiento de cierre / Sistema OT', activo: true },
  { id: 'p-ot-23', cuestionarioId: 'q-ot', numero: 23, texto: '¿El cierre de la orden incluye validación del trabajo realizado por parte del supervisor o usuario responsable?', evidencia: 'Firma de aprobación / Visto bueno / Sistema CMMS', activo: true },
  { id: 'p-ot-24', cuestionarioId: 'q-ot', numero: 24, texto: '¿La información de la orden alimenta el historial del equipo intervenido?', evidencia: 'Hoja de vida del equipo / Históricos / Base de datos', activo: true },
  { id: 'p-ot-25', cuestionarioId: 'q-ot', numero: 25, texto: '¿La empresa puede consultar fácilmente órdenes anteriores de un equipo específico?', evidencia: 'CMMS / ERP / Archivo físico organizado / Base histórica', activo: true },
  { id: 'p-ot-26', cuestionarioId: 'q-ot', numero: 26, texto: '¿Las órdenes de trabajo permiten analizar tiempos de parada, tiempos de reparación o reincidencia de fallas?', evidencia: 'Reportes KPI / MTTR / Históricos / Dashboard', activo: true },
  { id: 'p-ot-27', cuestionarioId: 'q-ot', numero: 27, texto: '¿La información de las órdenes de trabajo se utiliza para mejorar la planeación y la confiabilidad del mantenimiento?', evidencia: 'Informes de gestión / Reuniones de análisis / Indicadores', activo: true },
  { id: 'p-ot-28', cuestionarioId: 'q-ot', numero: 28, texto: '¿Existe trazabilidad completa desde la generación hasta el cierre de cada orden de trabajo?', evidencia: 'Flujo documental / Sistema OT / Auditoría de registros', activo: true },
];

const preguntasAlmacen1: Pregunta[] = [
  { id: 'p-alm1-1', cuestionarioId: 'q-almacen-1', numero: 1, texto: '¿Las partes de repuesto están organizadas y localizadas en un almacén?', evidencia: 'Inspección física', activo: true },
  { id: 'p-alm1-2', cuestionarioId: 'q-almacen-1', numero: 2, texto: '¿Las refacciones están identificadas con números de parte y ubicaciones específicas?', evidencia: 'Sistema / Etiquetado', activo: true },
  { id: 'p-alm1-3', cuestionarioId: 'q-almacen-1', numero: 3, texto: '¿Existe un método estructurado de localización (no solo búsqueda manual)?', evidencia: 'Procedimiento / Sistema', activo: true },
  { id: 'p-alm1-4', cuestionarioId: 'q-almacen-1', numero: 4, texto: '¿El almacén cuenta con gestión visual para identificar ubicaciones fácilmente?', evidencia: 'Señalización / Layout', activo: true },
  { id: 'p-alm1-5', cuestionarioId: 'q-almacen-1', numero: 5, texto: '¿Los repuestos críticos tienen sistemas visuales tipo Kanban para control de stock?', evidencia: 'Sistema visual / Kanban', activo: true },
  { id: 'p-alm1-6', cuestionarioId: 'q-almacen-1', numero: 6, texto: '¿Existe un listado actualizado de repuestos en inventario?', evidencia: 'Sistema / Kardex', activo: true },
  { id: 'p-alm1-7', cuestionarioId: 'q-almacen-1', numero: 7, texto: '¿Se clasifican los repuestos según criticidad (críticos, importantes, no críticos)?', evidencia: 'Matriz de criticidad', activo: true },
  { id: 'p-alm1-8', cuestionarioId: 'q-almacen-1', numero: 8, texto: '¿Se tienen definidos niveles mínimos y máximos de inventario?', evidencia: 'Políticas de stock', activo: true },
  { id: 'p-alm1-9', cuestionarioId: 'q-almacen-1', numero: 9, texto: '¿Se realiza control de inventario (entradas y salidas) de forma sistemática?', evidencia: 'Kardex / Sistema', activo: true },
  { id: 'p-alm1-10', cuestionarioId: 'q-almacen-1', numero: 10, texto: '¿Se realizan inventarios físicos periódicos?', evidencia: 'Actas de inventario', activo: true },
  { id: 'p-alm1-11', cuestionarioId: 'q-almacen-1', numero: 11, texto: '¿Se comparan los inventarios físicos vs registros del sistema?', evidencia: 'Conciliaciones', activo: true },
  { id: 'p-alm1-13', cuestionarioId: 'q-almacen-1', numero: 13, texto: '¿Se registra el consumo de repuestos por cada orden de trabajo?', evidencia: 'Órdenes de trabajo / Sistema', activo: true },
  { id: 'p-alm1-14', cuestionarioId: 'q-almacen-1', numero: 14, texto: '¿Existe trazabilidad entre repuestos y equipos donde se utilizan?', evidencia: 'Historial de mantenimiento', activo: true },
  { id: 'p-alm1-15', cuestionarioId: 'q-almacen-1', numero: 15, texto: '¿Se puede identificar qué repuesto fue usado, cuándo y por quién?', evidencia: 'Registros / Sistema', activo: true },
  { id: 'p-alm1-16', cuestionarioId: 'q-almacen-1', numero: 16, texto: '¿Se lleva historial de uso de repuestos por equipo?', evidencia: 'Base de datos / CMMS', activo: true },
  { id: 'p-alm1-17', cuestionarioId: 'q-almacen-1', numero: 17, texto: '¿Se analizan los repuestos de mayor consumo o criticidad?', evidencia: 'Reportes / Indicadores', activo: true },
  { id: 'p-alm1-18', cuestionarioId: 'q-almacen-1', numero: 18, texto: '¿Existe un proceso definido para la solicitud de repuestos?', evidencia: 'Procedimiento documentado', activo: true },
  { id: 'p-alm1-19', cuestionarioId: 'q-almacen-1', numero: 19, texto: '¿Las solicitudes de repuestos requieren aprobación antes de la compra?', evidencia: 'Formatos / Sistema', activo: true },
  { id: 'p-alm1-20', cuestionarioId: 'q-almacen-1', numero: 20, texto: '¿Se cuenta con proveedores homologados o evaluados?', evidencia: 'Base de proveedores', activo: true },
  { id: 'p-alm1-21', cuestionarioId: 'q-almacen-1', numero: 21, texto: '¿Se evalúa el desempeño de los proveedores (tiempo, calidad, costo)?', evidencia: 'Indicadores / Reportes', activo: true },
  { id: 'p-alm1-22', cuestionarioId: 'q-almacen-1', numero: 22, texto: '¿Se controlan los tiempos de entrega de los repuestos?', evidencia: 'Órdenes de compra / Seguimiento', activo: true },
  { id: 'p-alm1-23', cuestionarioId: 'q-almacen-1', numero: 23, texto: '¿Existe gestión de compras para repuestos críticos con prioridad?', evidencia: 'Procedimiento / Política', activo: true },
];

const preguntasAlmacen2: Pregunta[] = [
  { id: 'p-alm2-1', cuestionarioId: 'q-almacen-2', numero: 1, texto: '¿La organización identifica repuestos de lento movimiento (sin rotación en aproximadamente 2 años)?', evidencia: 'Reportes de inventario / rotación', activo: true },
  { id: 'p-alm2-2', cuestionarioId: 'q-almacen-2', numero: 2, texto: '¿Se cuenta con clasificación de inventarios según su nivel de rotación (alta, media, baja, obsoleta)?', evidencia: 'Clasificación ABC / reportes', activo: true },
  { id: 'p-alm2-3', cuestionarioId: 'q-almacen-2', numero: 3, texto: '¿La organización identifica repuestos obsoletos (sin movimiento en más de 3–4 años)?', evidencia: 'Reportes históricos / inventario', activo: true },
  { id: 'p-alm2-4', cuestionarioId: 'q-almacen-2', numero: 4, texto: '¿Existe un procedimiento definido para la gestión de repuestos obsoletos?', evidencia: 'Procedimiento / política', activo: true },
  { id: 'p-alm2-5', cuestionarioId: 'q-almacen-2', numero: 5, texto: '¿Se realizan análisis periódicos de obsolescencia del inventario?', evidencia: 'Informes / reportes periódicos', activo: true },
  { id: 'p-alm2-6', cuestionarioId: 'q-almacen-2', numero: 6, texto: '¿Se toman decisiones sobre disposición de repuestos obsoletos (venta, baja, reutilización)?', evidencia: 'Registros / actas / decisiones', activo: true },
  { id: 'p-alm2-7', cuestionarioId: 'q-almacen-2', numero: 7, texto: '¿La empresa cuenta con provisiones financieras para inventarios obsoletos?', evidencia: 'Estados financieros / políticas contables', activo: true },
  { id: 'p-alm2-8', cuestionarioId: 'q-almacen-2', numero: 8, texto: '¿Las provisiones se calculan y actualizan periódicamente?', evidencia: 'Reportes financieros / contabilidad', activo: true },
  { id: 'p-alm2-9', cuestionarioId: 'q-almacen-2', numero: 9, texto: '¿Los inventarios obsoletos son descargados del sistema contable cuando corresponde?', evidencia: 'Registros contables / ajustes', activo: true },
  { id: 'p-alm2-10', cuestionarioId: 'q-almacen-2', numero: 10, texto: '¿Existe alineación entre inventario físico y contable en relación con repuestos obsoletos?', evidencia: 'Conciliaciones / auditorías', activo: true },
  { id: 'p-alm2-11', cuestionarioId: 'q-almacen-2', numero: 11, texto: '¿Se analizan las causas de obsolescencia (cambios tecnológicos, equipos fuera de servicio, sobrestock)?', evidencia: 'Análisis / informes', activo: true },
  { id: 'p-alm2-12', cuestionarioId: 'q-almacen-2', numero: 12, texto: '¿Se implementan acciones para evitar generación de inventario obsoleto?', evidencia: 'Planes de acción / políticas', activo: true },
];

const preguntasCultura: Pregunta[] = [
  { id: 'p-cult-1', cuestionarioId: 'q-cultura', numero: 1, texto: '¿La alta dirección reconoce el mantenimiento como una función estratégica?', evidencia: 'Políticas / Reuniones / Plan estratégico', activo: true },
  { id: 'p-cult-2', cuestionarioId: 'q-cultura', numero: 2, texto: '¿El mantenimiento es considerado una inversión y no solo un costo?', evidencia: 'Presupuestos / Justificación de inversiones', activo: true },
  { id: 'p-cult-3', cuestionarioId: 'q-cultura', numero: 3, texto: '¿Existe alineación entre mantenimiento y los objetivos del negocio?', evidencia: 'Planes estratégicos / KPI', activo: true },
  { id: 'p-cult-4', cuestionarioId: 'q-cultura', numero: 4, texto: '¿El área de mantenimiento participa en la toma de decisiones operativas?', evidencia: 'Actas de reunión / Comités técnicos', activo: true },
  { id: 'p-cult-5', cuestionarioId: 'q-cultura', numero: 5, texto: '¿Se promueve la prevención de fallas en lugar de la reacción a las mismas?', evidencia: 'Programas preventivos / Indicadores', activo: true },
  { id: 'p-cult-6', cuestionarioId: 'q-cultura', numero: 6, texto: '¿Se fomenta el cuidado de los equipos por parte de operación?', evidencia: 'Programas TPM / Buenas prácticas', activo: true },
  { id: 'p-cult-7', cuestionarioId: 'q-cultura', numero: 7, texto: '¿Existe compromiso visible de la dirección con el mantenimiento?', evidencia: 'Iniciativas / Comunicaciones internas', activo: true },
  { id: 'p-cult-8', cuestionarioId: 'q-cultura', numero: 8, texto: '¿El personal reporta fallas de manera oportuna?', evidencia: 'Registros de reportes / Sistema OT', activo: true },
  { id: 'p-cult-9', cuestionarioId: 'q-cultura', numero: 9, texto: '¿Se siguen procedimientos establecidos para la ejecución de mantenimiento?', evidencia: 'Procedimientos / Auditorías internas', activo: true },
  { id: 'p-cult-10', cuestionarioId: 'q-cultura', numero: 10, texto: '¿El personal muestra disciplina en el registro de información?', evidencia: 'Órdenes de trabajo / Bitácoras', activo: true },
  { id: 'p-cult-11', cuestionarioId: 'q-cultura', numero: 11, texto: '¿Se fomenta la mejora continua en las actividades de mantenimiento?', evidencia: 'Propuestas / Reuniones / Kaizen', activo: true },
  { id: 'p-cult-12', cuestionarioId: 'q-cultura', numero: 12, texto: '¿Existe sentido de responsabilidad sobre los activos por parte del personal?', evidencia: 'Observación en campo / Evaluaciones', activo: true },
  { id: 'p-cult-13', cuestionarioId: 'q-cultura', numero: 13, texto: '¿Se promueve el trabajo en equipo entre mantenimiento y operación?', evidencia: 'Proyectos conjuntos / Reuniones', activo: true },
  { id: 'p-cult-14', cuestionarioId: 'q-cultura', numero: 14, texto: '¿El personal actúa de manera proactiva frente a posibles fallas?', evidencia: 'Reportes preventivos / Inspecciones', activo: true },
  { id: 'p-cult-15', cuestionarioId: 'q-cultura', numero: 15, texto: '¿Existe comunicación efectiva entre mantenimiento y otras áreas?', evidencia: 'Reuniones / Informes / Canales formales', activo: true },
  { id: 'p-cult-16', cuestionarioId: 'q-cultura', numero: 16, texto: '¿Se comparten lecciones aprendidas de fallas o intervenciones?', evidencia: 'Informes / Base de conocimiento', activo: true },
  { id: 'p-cult-17', cuestionarioId: 'q-cultura', numero: 17, texto: '¿Se analizan las fallas para evitar su repetición?', evidencia: 'RCA / Informes técnicos', activo: true },
  { id: 'p-cult-18', cuestionarioId: 'q-cultura', numero: 18, texto: '¿Se promueve la mejora continua en los procesos de mantenimiento?', evidencia: 'Planes de mejora / Seguimiento', activo: true },
  { id: 'p-cult-19', cuestionarioId: 'q-cultura', numero: 19, texto: '¿La empresa documenta y reutiliza el conocimiento técnico generado?', evidencia: 'Base de datos / CMMS / Manuales', activo: true },
  { id: 'p-cult-20', cuestionarioId: 'q-cultura', numero: 20, texto: '¿Se reconocen las buenas prácticas del personal de mantenimiento?', evidencia: 'Programas de reconocimiento', activo: true },
  { id: 'p-cult-21', cuestionarioId: 'q-cultura', numero: 21, texto: '¿Existe una cultura orientada a la confiabilidad de los activos?', evidencia: 'KPI / Estrategia / Indicadores', activo: true },
];

const preguntasInfo: Pregunta[] = [
  { id: 'p-info-1', cuestionarioId: 'q-info', numero: 1, texto: '¿La empresa tiene documentada la información básica de sus equipos (fabricante, modelo, serie, fecha de compra, proveedor, costo, etc.)?', evidencia: 'CMMS / Hojas de vida de equipos / Registro maestro', activo: true },
  { id: 'p-info-2', cuestionarioId: 'q-info', numero: 2, texto: '¿La información básica está documentada para todos los equipos y no solo para los críticos?', evidencia: 'Base de datos / Inventario técnico', activo: true },
  { id: 'p-info-3', cuestionarioId: 'q-info', numero: 3, texto: '¿Los equipos tienen asociado un listado de repuestos principales o críticos?', evidencia: 'BOM / Listado de repuestos / CMMS', activo: true },
  { id: 'p-info-4', cuestionarioId: 'q-info', numero: 4, texto: '¿La información técnica de los equipos (manuales, planos, catálogos, diagramas, fichas técnicas) se encuentra archivada en un lugar definido?', evidencia: 'Archivo físico / Carpeta digital / Repositorio técnico', activo: true },
  { id: 'p-info-5', cuestionarioId: 'q-info', numero: 5, texto: '¿Existe un sistema formal para custodiar, administrar y actualizar la información técnica de los equipos?', evidencia: 'Procedimiento / Sistema documental / Responsable asignado', activo: true },
  { id: 'p-info-6', cuestionarioId: 'q-info', numero: 6, texto: '¿La empresa revisa periódicamente la calidad y vigencia de la información técnica almacenada?', evidencia: 'Procedimiento de control documental / Registros de revisión', activo: true },
  { id: 'p-info-7', cuestionarioId: 'q-info', numero: 7, texto: '¿La organización mantiene historial de fallas, paros, intervenciones, repuestos utilizados y acciones correctivas por equipo?', evidencia: 'Historial de equipos / CMMS / Bitácoras', activo: true },
  { id: 'p-info-8', cuestionarioId: 'q-info', numero: 8, texto: '¿El historial de los equipos es accesible para el personal que lo necesita en operación, mantenimiento o ingeniería?', evidencia: 'Sistema / Accesos / Consulta en campo', activo: true },
  { id: 'p-info-9', cuestionarioId: 'q-info', numero: 9, texto: '¿La información histórica se utiliza para análisis, seguimiento del ciclo de vida y toma de decisiones sobre los activos?', evidencia: 'Reportes / Indicadores / Análisis de fallas', activo: true },
  { id: 'p-info-10', cuestionarioId: 'q-info', numero: 10, texto: '¿La información importante de mantenimiento y equipos está en sistemas formales y no depende de cuadernos o registros personales?', evidencia: 'Inspección documental / Entrevistas / Sistema', activo: true },
  { id: 'p-info-11', cuestionarioId: 'q-info', numero: 11, texto: '¿Los procedimientos, rutinas de mantenimiento, especificaciones y listas de materiales están integrados al sistema de información de la empresa?', evidencia: 'CMMS / Procedimientos / BOM / ERP', activo: true },
  { id: 'p-info-12', cuestionarioId: 'q-info', numero: 12, texto: '¿La empresa cuenta con materiales formales para transferencia de conocimiento, como procedimientos, instructivos, ayudas visuales o lecciones aprendidas?', evidencia: 'Procedimientos / Instructivos / LUP / Guías de trabajo', activo: true },
  { id: 'p-info-13', cuestionarioId: 'q-info', numero: 13, texto: '¿La experiencia del personal que opera y mantiene los equipos se documenta para evitar que el conocimiento quede solo en las personas?', evidencia: 'Entrevistas / Procedimientos / Lecciones aprendida', activo: true },
  { id: 'p-info-14', cuestionarioId: 'q-info', numero: 14, texto: '¿La información necesaria para operar, mantener o limpiar los equipos está documentada en formatos consistentes y disponibles para consulta?', evidencia: 'Procedimientos estandarizados / Intranet / Carpeta técnica', activo: true },
  { id: 'p-info-15', cuestionarioId: 'q-info', numero: 15, texto: '¿Los datos de desempeño de los equipos (temperatura, vibración, presión, ciclos, etc.) son recolectados y almacenados de forma sistemática?', evidencia: 'Sistema de monitoreo / CMMS / Históricos', activo: true },
  { id: 'p-info-16', cuestionarioId: 'q-info', numero: 16, texto: '¿La información de desempeño se utiliza para análisis de causa raíz, mantenimiento preventivo o predictivo y toma de decisiones?', evidencia: 'Reportes / RCA / Tendencias / Alarmas', activo: true },
  { id: 'p-info-17', cuestionarioId: 'q-info', numero: 17, texto: '¿Las solicitudes de mantenimiento, horas trabajadas, repuestos consumidos y tipos de trabajo se registran formalmente en el CMMS?', evidencia: 'Órdenes de trabajo / CMMS / Reportes', activo: true },
  { id: 'p-info-18', cuestionarioId: 'q-info', numero: 18, texto: '¿La empresa tiene documentado el control de interfaces, PLC, software de control, respaldos y administración de cambios?', evidencia: 'Procedimiento / Backups / Versionamiento / Registros', activo: true },
  { id: 'p-info-19', cuestionarioId: 'q-info', numero: 19, texto: '¿Los manuales, programas y documentación de sistemas de control están disponibles y protegidos contra pérdida o daño?', evidencia: 'Archivo técnico / Copias de seguridad / Control documental', activo: true },
  { id: 'p-info-20', cuestionarioId: 'q-info', numero: 20, texto: '¿Existe un sistema de recolección de datos que apoye el análisis de pérdidas, costos, oportunidades de mejora y procesos de mejora continua?', evidencia: 'TPM / Indicadores / Tableros / Reportes', activo: true },
];

const preguntasHabilidades: Pregunta[] = [
  { id: 'p-hab-1', cuestionarioId: 'q-habilidades', numero: 1, texto: '¿La empresa tiene definidas las competencias requeridas para el personal de mantenimiento?', evidencia: 'Perfil de cargos / Matriz de competencias', activo: true },
  { id: 'p-hab-2', cuestionarioId: 'q-habilidades', numero: 2, texto: '¿Se evalúan las habilidades técnicas del personal de manera periódica?', evidencia: 'Evaluaciones / Pruebas técnicas / Registros', activo: true },
  { id: 'p-hab-3', cuestionarioId: 'q-habilidades', numero: 3, texto: '¿Existe una matriz de habilidades del personal actualizada?', evidencia: 'Matriz de competencias / Base de datos', activo: true },
  { id: 'p-hab-4', cuestionarioId: 'q-habilidades', numero: 4, texto: '¿Se identifican brechas entre las competencias requeridas y las existentes?', evidencia: 'Análisis de brechas / Informes', activo: true },
  { id: 'p-hab-5', cuestionarioId: 'q-habilidades', numero: 5, texto: '¿Se diferencian niveles de experiencia (junior, senior, especialista)?', evidencia: 'Clasificación de personal / Organigrama técnico', activo: true },
  { id: 'p-hab-6', cuestionarioId: 'q-habilidades', numero: 6, texto: '¿Se evalúan habilidades prácticas además de conocimientos teóricos?', evidencia: 'Pruebas en campo / Evaluaciones prácticas', activo: true },
  { id: 'p-hab-7', cuestionarioId: 'q-habilidades', numero: 7, texto: '¿Se documentan los resultados de las evaluaciones de habilidades?', evidencia: 'Registros / Informes de evaluación', activo: true },
  { id: 'p-hab-8', cuestionarioId: 'q-habilidades', numero: 8, texto: '¿Existe un plan de capacitación para el personal de mantenimiento?', evidencia: 'Plan anual de formación / Cronograma', activo: true },
  { id: 'p-hab-9', cuestionarioId: 'q-habilidades', numero: 9, texto: '¿Las capacitaciones están alineadas con las necesidades del área de mantenimiento?', evidencia: 'Plan de capacitación / Análisis de brechas', activo: true },
  { id: 'p-hab-10', cuestionarioId: 'q-habilidades', numero: 10, texto: '¿Se realizan capacitaciones técnicas específicas por tipo de equipo?', evidencia: 'Registros de formación / Certificados', activo: true },
  { id: 'p-hab-11', cuestionarioId: 'q-habilidades', numero: 11, texto: '¿El personal recibe capacitación en seguridad industrial?', evidencia: 'Certificados / Registros SST', activo: true },
  { id: 'p-hab-12', cuestionarioId: 'q-habilidades', numero: 12, texto: '¿Se evalúa la efectividad de las capacitaciones realizadas?', evidencia: 'Evaluaciones post-capacitación / Seguimiento', activo: true },
  { id: 'p-hab-13', cuestionarioId: 'q-habilidades', numero: 13, texto: '¿Se promueve la formación continua del personal?', evidencia: 'Programas de desarrollo / Cursos / Entrenamientos', activo: true },
  { id: 'p-hab-14', cuestionarioId: 'q-habilidades', numero: 14, texto: '¿Se documenta el historial de capacitación de cada trabajador?', evidencia: 'Registro individual / Base de datos', activo: true },
  { id: 'p-hab-15', cuestionarioId: 'q-habilidades', numero: 15, texto: '¿El personal aplica correctamente sus habilidades en la ejecución de órdenes de trabajo?', evidencia: 'Observación en campo / OT ejecutadas', activo: true },
  { id: 'p-hab-16', cuestionarioId: 'q-habilidades', numero: 16, texto: '¿Se supervisa el desempeño técnico del personal en campo?', evidencia: 'Informes de supervisión / Evaluaciones', activo: true },
  { id: 'p-hab-17', cuestionarioId: 'q-habilidades', numero: 17, texto: '¿Se asignan tareas según el nivel de competencia del personal?', evidencia: 'Asignación de OT / Programación', activo: true },
  { id: 'p-hab-18', cuestionarioId: 'q-habilidades', numero: 18, texto: '¿Se identifican errores o fallas atribuibles a falta de habilidades?', evidencia: 'Reportes de fallas / Análisis de causas', activo: true },
  { id: 'p-hab-19', cuestionarioId: 'q-habilidades', numero: 19, texto: '¿Se implementan acciones para mejorar las competencias detectadas como débiles?', evidencia: 'Planes de mejora / Reentrenamiento', activo: true },
  { id: 'p-hab-20', cuestionarioId: 'q-habilidades', numero: 20, texto: '¿Existe transferencia de conocimiento entre personal experimentado y nuevo?', evidencia: 'Programas de mentoría / Capacitación interna', activo: true },
  { id: 'p-hab-21', cuestionarioId: 'q-habilidades', numero: 21, texto: '¿La empresa mide el impacto de las habilidades en el desempeño del mantenimiento?', evidencia: 'KPI / Indicadores de desempeño', activo: true },
];

const preguntasMediciones: Pregunta[] = [
  { id: 'p-med-1', cuestionarioId: 'q-mediciones', numero: 1, texto: '¿La empresa cuenta con indicadores definidos para medir el desempeño del mantenimiento?', evidencia: 'KPI definidos / Cuadro de mando / Sistema CMMS', activo: true },
  { id: 'p-med-2', cuestionarioId: 'q-mediciones', numero: 2, texto: '¿Se realiza seguimiento periódico a los indicadores de mantenimiento?', evidencia: 'Reportes mensuales / Informes de gestión', activo: true },
  { id: 'p-med-3', cuestionarioId: 'q-mediciones', numero: 3, texto: '¿Se miden indicadores como disponibilidad, confiabilidad o cumplimiento del plan?', evidencia: 'KPI (Disponibilidad, MTBF, cumplimiento PM)', activo: true },
  { id: 'p-med-4', cuestionarioId: 'q-mediciones', numero: 4, texto: '¿Se monitorea el porcentaje de mantenimiento correctivo vs preventivo?', evidencia: 'Reportes CMMS / Indicadores de mantenimiento', activo: true },
  { id: 'p-med-5', cuestionarioId: 'q-mediciones', numero: 5, texto: '¿Se evalúa el cumplimiento de las órdenes de trabajo programadas?', evidencia: 'Programación vs ejecución / Reportes', activo: true },
  { id: 'p-med-6', cuestionarioId: 'q-mediciones', numero: 6, texto: '¿Los indicadores son conocidos por el equipo de mantenimiento?', evidencia: 'Reuniones / Tableros visuales / Informes', activo: true },
  { id: 'p-med-7', cuestionarioId: 'q-mediciones', numero: 7, texto: '¿Se utilizan los indicadores para la toma de decisiones?', evidencia: 'Actas de reunión / Planes de acción', activo: true },
  { id: 'p-med-8', cuestionarioId: 'q-mediciones', numero: 8, texto: '¿Se realizan mediciones técnicas periódicas a los equipos (vibración, temperatura, etc.)?', evidencia: 'Registros de monitoreo / Informes técnicos', activo: true },
  { id: 'p-med-9', cuestionarioId: 'q-mediciones', numero: 9, texto: '¿Existen instrumentos adecuados para la medición de condiciones de operación?', evidencia: 'Equipos de medición / Inventario de instrumentos', activo: true },
  { id: 'p-med-10', cuestionarioId: 'q-mediciones', numero: 10, texto: '¿Los instrumentos de medición están calibrados y certificados?', evidencia: 'Certificados de calibración / Registros', activo: true },
  { id: 'p-med-11', cuestionarioId: 'q-mediciones', numero: 11, texto: '¿Se registran los resultados de las mediciones en un sistema o formato?', evidencia: 'Bases de datos / Hojas de registro', activo: true },
  { id: 'p-med-12', cuestionarioId: 'q-mediciones', numero: 12, texto: '¿Se analizan tendencias de las variables medidas en los equipos?', evidencia: 'Gráficos / Software / Históricos', activo: true },
  { id: 'p-med-13', cuestionarioId: 'q-mediciones', numero: 13, texto: '¿Se utilizan las mediciones para anticipar fallas (mantenimiento predictivo)?', evidencia: 'Informes predictivos / Alarmas / CMMS', activo: true },
  { id: 'p-med-14', cuestionarioId: 'q-mediciones', numero: 14, texto: '¿Existe un programa estructurado de monitoreo de condición?', evidencia: 'Plan de mantenimiento predictivo', activo: true },
  { id: 'p-med-15', cuestionarioId: 'q-mediciones', numero: 15, texto: '¿Se analizan los resultados de los indicadores de mantenimiento?', evidencia: 'Informes / Reuniones técnicas', activo: true },
  { id: 'p-med-16', cuestionarioId: 'q-mediciones', numero: 16, texto: '¿Se identifican causas raíz de desviaciones en los indicadores?', evidencia: 'Análisis RCA / Reportes técnicos', activo: true },
  { id: 'p-med-17', cuestionarioId: 'q-mediciones', numero: 17, texto: '¿Se generan planes de acción basados en los resultados de las mediciones?', evidencia: 'Planes de mejora / Seguimiento', activo: true },
  { id: 'p-med-18', cuestionarioId: 'q-mediciones', numero: 18, texto: '¿Se evalúa la efectividad de las acciones implementadas?', evidencia: 'Indicadores antes/después / Reportes', activo: true },
  { id: 'p-med-19', cuestionarioId: 'q-mediciones', numero: 19, texto: '¿Existe retroalimentación continua hacia el proceso de mantenimiento?', evidencia: 'Reuniones / Ajustes de planes', activo: true },
  { id: 'p-med-20', cuestionarioId: 'q-mediciones', numero: 20, texto: '¿Se integran las mediciones con la planificación del mantenimiento?', evidencia: 'CMMS / Programación basada en datos', activo: true },
  { id: 'p-med-21', cuestionarioId: 'q-mediciones', numero: 21, texto: '¿La empresa utiliza las mediciones para mejorar la confiabilidad de los activos?', evidencia: 'Informes de confiabilidad / KPI', activo: true },
];

export const preguntasPorCuestionario: Record<string, Pregunta[]> = {
  'q-estrategia': preguntasEstrategia,
  'q-ot': preguntasOT,
  'q-almacen-1': preguntasAlmacen1,
  'q-almacen-2': preguntasAlmacen2,
  'q-cultura': preguntasCultura,
  'q-info': preguntasInfo,
  'q-habilidades': preguntasHabilidades,
  'q-mediciones': preguntasMediciones,
};

// numPreguntas se deriva del conteo real, igual que el trigger fn_sync_num_preguntas
// de la base de datos — nunca se declara a mano.
cuestionarios.forEach((c) => {
  c.numPreguntas = (preguntasPorCuestionario[c.id] ?? []).length;
});

export const auditorias: Auditoria[] = [];

export const reportes: Reporte[] = [];

export const hallazgos: Hallazgo[] = [];
