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

export const empresas: Empresa[] = [
  { id: 'e-1', razonSocial: 'Industrias Palma SAS', nit: '900.123.456-7', sector: 'Manufactura', numEmpleados: 62, ciudad: 'Cali', departamento: 'Valle del Cauca', auditoriasRealizadas: 4, ultimaVisita: '2026-06-07' },
  { id: 'e-2', razonSocial: 'Textiles del Valle', nit: '900.234.567-8', sector: 'Manufactura', numEmpleados: 38, ciudad: 'Palmira', departamento: 'Valle del Cauca', auditoriasRealizadas: 2, ultimaVisita: '2026-06-05' },
  { id: 'e-3', razonSocial: 'Alimentos Frescos Ltda', nit: '900.345.678-9', sector: 'Agroindustria', numEmpleados: 25, ciudad: 'Cali', departamento: 'Valle del Cauca', auditoriasRealizadas: 3, ultimaVisita: '2026-06-02' },
  { id: 'e-4', razonSocial: 'Metalmec S.A.', nit: '900.456.789-0', sector: 'Manufactura', numEmpleados: 88, ciudad: 'Buenaventura', departamento: 'Valle del Cauca', auditoriasRealizadas: 1, ultimaVisita: '2026-05-28' },
];

export const categorias: Categoria[] = [
  { id: 'cat-mant', nombre: 'Mantenimiento', icono: 'ti-settings', esPlantilla: true, activo: true },
  { id: 'cat-admin', nombre: 'Administración', icono: 'ti-briefcase', esPlantilla: true, activo: true },
  { id: 'cat-fin', nombre: 'Finanzas', icono: 'ti-report-money', esPlantilla: true, activo: true },
  { id: 'cat-prod', nombre: 'Producción', icono: 'ti-truck', esPlantilla: true, activo: true },
  { id: 'cat-cal', nombre: 'Calidad', icono: 'ti-certificate', esPlantilla: true, activo: true },
  { id: 'cat-com', nombre: 'Comercial', icono: 'ti-building-store', esPlantilla: true, activo: true },
];

export const subcategorias: Subcategoria[] = [
  { id: 'sub-ot', categoriaId: 'cat-mant', nombre: 'Órdenes de trabajo', esPlantilla: true, activo: true },
  { id: 'sub-med', categoriaId: 'cat-mant', nombre: 'Mediciones', esPlantilla: true, activo: true },
  { id: 'sub-rrhh', categoriaId: 'cat-admin', nombre: 'Recursos humanos', esPlantilla: true, activo: true },
  { id: 'sub-cont', categoriaId: 'cat-fin', nombre: 'Contabilidad y costos', esPlantilla: true, activo: true },
  { id: 'sub-fzv', categoriaId: 'cat-com', nombre: 'Fuerza de ventas', esPlantilla: true, activo: true },
];

export const cuestionarios: Cuestionario[] = [
  { id: 'q-ot-1', subcategoriaId: 'sub-ot', nombre: 'Generación y control', numPreguntas: 0, activo: true },
  { id: 'q-med-1', subcategoriaId: 'sub-med', nombre: 'Indicadores de desempeño', numPreguntas: 0, activo: true },
  { id: 'q-rrhh-1', subcategoriaId: 'sub-rrhh', nombre: 'Evaluación de desempeño', numPreguntas: 0, activo: true },
  { id: 'q-cont-1', subcategoriaId: 'sub-cont', nombre: 'Costos de mantenimiento', numPreguntas: 0, activo: true },
  { id: 'q-fzv-1', subcategoriaId: 'sub-fzv', nombre: 'Gestión comercial', numPreguntas: 0, activo: true },
];

// Preguntas reales tomadas del prototipo (auditapp.html), agrupadas por módulo.
export const preguntasOT: Pregunta[] = [
  { id: 'p-ot-1', cuestionarioId: 'q-ot-1', numero: 1, texto: '¿La empresa cuenta con un formato estandarizado para la generación de órdenes de trabajo?', evidencia: 'Formato físico o digital de OT / CMMS / ERP', activo: true },
  { id: 'p-ot-2', cuestionarioId: 'q-ot-1', numero: 2, texto: '¿Cada orden de trabajo se genera de manera formal y no solo verbalmente?', evidencia: 'Procedimiento interno / Registro de solicitudes / Sistema', activo: true },
  { id: 'p-ot-3', cuestionarioId: 'q-ot-1', numero: 3, texto: '¿Cada orden de trabajo tiene un código o consecutivo único para su trazabilidad?', evidencia: 'Numeración consecutiva / Sistema OT / Base de datos', activo: true },
  { id: 'p-ot-4', cuestionarioId: 'q-ot-1', numero: 4, texto: '¿Las órdenes de trabajo registran fecha de creación y fecha programada de ejecución?', evidencia: 'Orden de trabajo diligenciada / Software CMMS', activo: true },
  { id: 'p-ot-5', cuestionarioId: 'q-ot-1', numero: 5, texto: '¿La orden de trabajo identifica claramente el equipo, activo o sistema a intervenir?', evidencia: 'Campo de activo / Código de equipo / Hoja de vida del equipo', activo: true },
  { id: 'p-ot-6', cuestionarioId: 'q-ot-1', numero: 6, texto: '¿Las órdenes clasifican el tipo de intervención (preventiva, correctiva, predictiva, mejora, inspección)?', evidencia: 'Formato OT / Catálogo de tipos de mantenimiento / Sistema', activo: true },
  { id: 'p-ot-7', cuestionarioId: 'q-ot-1', numero: 7, texto: '¿Las órdenes de trabajo contemplan prioridad o nivel de criticidad?', evidencia: 'Campo de prioridad / Matriz de criticidad / CMMS', activo: true },
  { id: 'p-ot-8', cuestionarioId: 'q-ot-1', numero: 8, texto: '¿Las órdenes de trabajo son revisadas y planificadas antes de su ejecución?', evidencia: 'Plan semanal / Programación / Backlog de mantenimiento', activo: true },
  { id: 'p-ot-9', cuestionarioId: 'q-ot-1', numero: 9, texto: '¿Se asigna un técnico o responsable específico para ejecutar cada orden?', evidencia: 'Campo responsable / Programación de personal / Sistema OT', activo: true },
  { id: 'p-ot-10', cuestionarioId: 'q-ot-1', numero: 10, texto: '¿La orden de trabajo incluye una descripción clara de la labor a realizar?', evidencia: 'Orden diligenciada / Instrucción técnica / Aviso de mantenimiento', activo: true },
  { id: 'p-ot-15', cuestionarioId: 'q-ot-1', numero: 15, texto: '¿Las órdenes registran la fecha y hora real de inicio de la intervención?', evidencia: 'OT cerrada / Sistema CMMS / Parte de trabajo', activo: true },
  { id: 'p-ot-18', cuestionarioId: 'q-ot-1', numero: 18, texto: '¿Se registran las fallas encontradas o condiciones observadas durante el trabajo?', evidencia: 'Descripción de falla / Diagnóstico / Reporte técnico', activo: true },
  { id: 'p-ot-22', cuestionarioId: 'q-ot-1', numero: 22, texto: '¿Las órdenes de trabajo son cerradas formalmente una vez ejecutadas?', evidencia: 'Estado cerrada / Procedimiento de cierre / Sistema OT', activo: true },
  { id: 'p-ot-26', cuestionarioId: 'q-ot-1', numero: 26, texto: '¿Las órdenes de trabajo permiten analizar tiempos de parada, tiempos de reparación o reincidencia de fallas?', evidencia: 'Reportes KPI / MTTR / Históricos / Dashboard', activo: true },
  { id: 'p-ot-28', cuestionarioId: 'q-ot-1', numero: 28, texto: '¿Existe trazabilidad completa desde la generación hasta el cierre de cada orden de trabajo?', evidencia: 'Flujo documental / Sistema OT / Auditoría de registros', activo: true },
];

export const preguntasMediciones: Pregunta[] = [
  { id: 'p-med-1', cuestionarioId: 'q-med-1', numero: 1, texto: '¿La empresa cuenta con indicadores definidos para medir el desempeño del mantenimiento?', evidencia: 'KPI definidos / Cuadro de mando / Sistema CMMS', activo: true },
  { id: 'p-med-2', cuestionarioId: 'q-med-1', numero: 2, texto: '¿Se realiza seguimiento periódico a los indicadores de mantenimiento?', evidencia: 'Reportes mensuales / Informes de gestión', activo: true },
  { id: 'p-med-3', cuestionarioId: 'q-med-1', numero: 3, texto: '¿Se miden indicadores como disponibilidad, confiabilidad o cumplimiento del plan?', evidencia: 'KPI (Disponibilidad, MTBF, cumplimiento PM)', activo: true },
  { id: 'p-med-8', cuestionarioId: 'q-med-1', numero: 8, texto: '¿Se realizan mediciones técnicas periódicas a los equipos (vibración, temperatura, etc.)?', evidencia: 'Registros de monitoreo / Informes técnicos', activo: true },
  { id: 'p-med-13', cuestionarioId: 'q-med-1', numero: 13, texto: '¿Se utilizan las mediciones para anticipar fallas (mantenimiento predictivo)?', evidencia: 'Informes predictivos / Alarmas / CMMS', activo: true },
  { id: 'p-med-16', cuestionarioId: 'q-med-1', numero: 16, texto: '¿Se identifican causas raíz de desviaciones en los indicadores?', evidencia: 'Análisis RCA / Reportes técnicos', activo: true },
  { id: 'p-med-21', cuestionarioId: 'q-med-1', numero: 21, texto: '¿La empresa utiliza las mediciones para mejorar la confiabilidad de los activos?', evidencia: 'Informes de confiabilidad / KPI', activo: true },
];

export const preguntasPorCuestionario: Record<string, Pregunta[]> = {
  'q-ot-1': preguntasOT,
  'q-med-1': preguntasMediciones,
};

// numPreguntas se deriva del conteo real, igual que el trigger fn_sync_num_preguntas
// de la base de datos — nunca se declara a mano.
cuestionarios.forEach((c) => {
  c.numPreguntas = (preguntasPorCuestionario[c.id] ?? []).length;
});

export const auditorias: Auditoria[] = [
  { id: 'a-1', empresaId: 'e-1', auditorId: 'u-1', estado: 'finalizada', puntajeGlobal: 3.8, fechaInicio: '2026-06-07', fechaFin: '2026-06-07' },
  { id: 'a-2', empresaId: 'e-2', auditorId: 'u-1', estado: 'en_progreso', puntajeGlobal: 2.1, fechaInicio: '2026-06-05' },
  { id: 'a-3', empresaId: 'e-3', auditorId: 'u-1', estado: 'finalizada', puntajeGlobal: 4.2, fechaInicio: '2026-06-02', fechaFin: '2026-06-02' },
  { id: 'a-4', empresaId: 'e-4', auditorId: 'u-1', estado: 'en_progreso', fechaInicio: '2026-05-28' },
];

export const reportes: Reporte[] = [
  { id: 'r-1', auditoriaId: 'a-1', puntajeTotal: 3.8, nivelMadurez: 'Intermedio', generadoEn: '2026-06-07' },
  { id: 'r-2', auditoriaId: 'a-3', puntajeTotal: 1.9, nivelMadurez: 'Básico/reactivo', generadoEn: '2026-06-02' },
  { id: 'r-3', auditoriaId: 'a-2', puntajeTotal: 4.2, nivelMadurez: 'Avanzado', generadoEn: '2026-05-28' },
];

export const hallazgos: Hallazgo[] = [
  { id: 'h-1', auditoriaId: 'a-1', descripcion: 'No existe sistema formal de OT', severidad: 'critica', accionRecomendada: 'Implementar CMMS o formato digital', estado: 'abierto', area: 'Mantenimiento' },
  { id: 'h-2', auditoriaId: 'a-1', descripcion: 'OT sin consecutivo único', severidad: 'critica', accionRecomendada: 'Definir numeración consecutiva', estado: 'abierto', area: 'Mantenimiento' },
  { id: 'h-3', auditoriaId: 'a-1', descripcion: 'Sin indicadores de desempeño', severidad: 'critica', accionRecomendada: 'Definir KPIs mínimos (disponibilidad, MTBF)', estado: 'abierto', area: 'Mantenimiento' },
  { id: 'h-4', auditoriaId: 'a-1', descripcion: 'Costos sin estructura clara', severidad: 'media', accionRecomendada: 'Separar costos correctivos vs. preventivos', estado: 'en_tratamiento', area: 'Finanzas' },
  { id: 'h-5', auditoriaId: 'a-1', descripcion: 'OT solo verbal en algunos casos', severidad: 'media', accionRecomendada: 'Formalizar todas las solicitudes', estado: 'abierto', area: 'Mantenimiento' },
  { id: 'h-6', auditoriaId: 'a-1', descripcion: 'Sin análisis de causa raíz', severidad: 'media', accionRecomendada: 'Implementar metodología RCA básica', estado: 'abierto', area: 'Mantenimiento' },
  { id: 'h-7', auditoriaId: 'a-1', descripcion: 'Registros de costos incompletos', severidad: 'baja', accionRecomendada: 'Actualizar registros mensuales', estado: 'cerrado', area: 'Finanzas' },
  { id: 'h-8', auditoriaId: 'a-1', descripcion: 'Sin plan de calibración', severidad: 'baja', accionRecomendada: 'Elaborar cronograma de calibración', estado: 'en_tratamiento', area: 'Mantenimiento' },
];
