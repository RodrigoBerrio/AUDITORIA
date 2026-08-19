// ============================================================
// Tipos de dominio — derivados 1:1 del esquema PostgreSQL v3
// (tablas.md). Los union types reflejan los CHECK constraints
// de la base de datos: si el backend rechaza un valor, TS ya
// lo habría marcado en rojo antes de compilar.
// ============================================================

export type Rol = 'auditor' | 'admin' | 'supervisor';

export type EstadoAuditoria = 'en_progreso' | 'finalizada' | 'cancelada';

export type EstadoCuestionario = 'pendiente' | 'en_progreso' | 'completado';

export type Severidad = 'baja' | 'media' | 'alta' | 'critica';

export type EstadoHallazgo = 'abierto' | 'en_tratamiento' | 'cerrado';

/** Valor de respuesta en la escala de madurez 1-5 usada en todo el formulario. */
export type ValorEscala = 1 | 2 | 3 | 4 | 5;

export interface Usuario {
  id: string;
  nombre: string;
  correo: string;
  rol: Rol;
  activo: boolean;
}

export interface Empresa {
  id: string;
  razonSocial: string;
  nit: string;
  sector?: string;
  numEmpleados?: number;
  ciudad?: string;
  departamento?: string;
  codigoPostal?: string;
  contacto?: string;
  telefono?: string;
  correo?: string;
  descripcion?: string;
  auditoriasRealizadas: number;
  ultimaVisita?: string;
}

export interface Categoria {
  id: string;
  nombre: string;
  descripcion?: string;
  icono?: string;
  esPlantilla: boolean;
  activo: boolean;
}

export interface Subcategoria {
  id: string;
  categoriaId: string;
  nombre: string;
  descripcion?: string;
  responsable?: string;
  esPlantilla: boolean;
  activo: boolean;
}

export interface Cuestionario {
  id: string;
  subcategoriaId: string;
  nombre: string;
  numPreguntas: number;
  activo: boolean;
}

export interface Pregunta {
  id: string;
  cuestionarioId: string;
  numero: number;
  texto: string;
  evidencia?: string;
  seccion?: string;
  activo: boolean;
}

export interface Auditoria {
  id: string;
  empresaId: string;
  auditorId: string;
  estado: EstadoAuditoria;
  puntajeGlobal?: number;
  fechaInicio: string;
  fechaFin?: string;
}

export interface AuditoriaCuestionario {
  id: string;
  auditoriaId: string;
  cuestionarioId: string;
  estado: EstadoCuestionario;
  puntaje?: number;
}

export interface Respuesta {
  id: string;
  auditoriaCuestionarioId: string;
  preguntaId: string;
  valor: ValorEscala;
  observacion?: string;
}

/** alcanceTipo/alcanceNombre null = informe integral (toda la auditoría); "categoria"/"subcategoria" = informe independiente de ese alcance (Etapa 5). */
export interface Reporte {
  id: string;
  auditoriaId: string;
  puntajeTotal?: number;
  nivelMadurez?: string;
  rutaPdf?: string;
  generadoEn: string;
  alcanceTipo?: 'categoria' | 'subcategoria';
  alcanceNombre?: string;
}

export interface Hallazgo {
  id: string;
  auditoriaId: string;
  descripcion: string;
  severidad: Severidad;
  accionRecomendada?: string;
  estado: EstadoHallazgo;
  area?: string;
}

export interface EvidenciaFotografica {
  id: string;
  auditoriaId: string;
  respuestaId?: string;
  urlArchivo: string;
  descripcion?: string;
  subidaPor: string;
  clienteUuid: string;
  tomadaEn?: string;
  subidaEn: string;
}

// ============================================================
// Respuestas de los endpoints agregados de resultados gráficos
// (GET /api/auditorias/:id/resumen|ranking|.../secciones|hallazgos/resumen,
// GET /api/empresas/:id/historico). Ya vienen agregados y con el color de
// semáforo calculado en backend — el front nunca recalcula el umbral.
// ============================================================

/**
 * puntaje null + evaluada false = subcategoría del catálogo todavía sin completar (plano
 * cartesiano vacío), no un puntaje de cero. detalle: desglose interno (por sección del/los
 * cuestionario(s)), vacío mientras evaluada=false. enAlcance: alcance inferido (no declarado) —
 * true en cuanto se aplicó al menos un cuestionario, distingue "no iniciada" de "en progreso".
 */
export interface PuntajeSubcategoria {
  subcategoria: string;
  categoria: string;
  puntaje: number | null;
  meta: number;
  colorSemaforo: string;
  evaluada: boolean;
  detalle: ItemPuntaje[];
  enAlcance: boolean;
}

/** Agregado por categoría: solo trae puntaje cuando completa=true — su histograma y radar propios solo se generan al completar todas sus subcategorías. subcategoriasEnAlcance distingue "sin empezar" de "en progreso" mientras nada está completo. */
export interface PuntajeCategoria {
  categoria: string;
  puntaje: number | null;
  colorSemaforo: string;
  completa: boolean;
  subcategoriasCompletas: number;
  subcategoriasTotal: number;
  subcategoriasEnAlcance: number;
}

export interface ResumenAuditoria {
  puntajeGlobal: number | null;
  nivelMadurez: string | null;
  colorSemaforo: string;
  subcategorias: PuntajeSubcategoria[];
  preguntasRespondidas: number;
  preguntasEsperadas: number;
  categorias: PuntajeCategoria[];
  catalogoCompleto: boolean;
  subcategoriasEvaluadas: number;
  subcategoriasTotal: number;
  subcategoriasEnAlcance: number;
}

export interface ItemPuntaje {
  etiqueta: string;
  puntaje: number | null;
  colorSemaforo: string;
  evaluada: boolean;
}

export interface RankingResultado {
  items: ItemPuntaje[];
  preguntasRespondidas: number;
  preguntasEsperadas: number;
}

export interface ConteoSeveridad {
  severidad: Severidad;
  cantidad: number;
  porcentaje: number;
  colorHex: string;
}

export interface ConteoEstado {
  estado: EstadoHallazgo;
  cantidad: number;
  porcentaje: number;
}

export interface HallazgosResumen {
  total: number;
  porSeveridad: ConteoSeveridad[];
  porEstado: ConteoEstado[];
}

export interface HistoricoPunto {
  fechaFin: string;
  puntajeGlobal: number | null;
}

/** Escala de madurez 1-5 con su etiqueta y descripción, tal como en el prototipo. */
export const ESCALA_MADUREZ: { valor: ValorEscala; etiqueta: string; descripcion: string }[] = [
  { valor: 1, etiqueta: 'De Falla', descripcion: 'No existe una estrategia claramente definida y documentada.' },
  { valor: 2, etiqueta: 'Reactivo', descripcion: 'Hay una estrategia clara y documentada, se ha dado a conocer y es bien entendida por todas las personas de mantenimiento.' },
  { valor: 3, etiqueta: 'Preventivo', descripcion: 'La estrategia tiene criterios medibles y cuantificables (SQCDM), que permiten monitorear su cumplimiento en el tiempo.' },
  { valor: 4, etiqueta: 'Predictivo', descripcion: 'Además estos criterios son monitoreados, exhibidos y la responsabilidad ha sido desplegada a todos en mantenimiento.' },
  { valor: 5, etiqueta: 'Mejores Prácticas', descripcion: 'Es evidente el cumplimiento de la estrategia y los indicadores confirman que las acciones tomadas han sido efectivas.' },
];
