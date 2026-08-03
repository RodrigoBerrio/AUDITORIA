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

export interface Reporte {
  id: string;
  auditoriaId: string;
  puntajeTotal?: number;
  nivelMadurez?: string;
  rutaPdf?: string;
  generadoEn: string;
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

/** Escala de madurez 1-5 con su etiqueta y descripción, tal como en el prototipo. */
export const ESCALA_MADUREZ: { valor: ValorEscala; etiqueta: string; descripcion: string }[] = [
  { valor: 1, etiqueta: 'De Falla', descripcion: 'No existe una estrategia claramente definida y documentada.' },
  { valor: 2, etiqueta: 'Reactivo', descripcion: 'Hay una estrategia clara y documentada, se ha dado a conocer y es bien entendida por todas las personas de mantenimiento.' },
  { valor: 3, etiqueta: 'Preventivo', descripcion: 'La estrategia tiene criterios medibles y cuantificables (SQCDM), que permiten monitorear su cumplimiento en el tiempo.' },
  { valor: 4, etiqueta: 'Predictivo', descripcion: 'Además estos criterios son monitoreados, exhibidos y la responsabilidad ha sido desplegada a todos en mantenimiento.' },
  { valor: 5, etiqueta: 'Mejores Prácticas', descripcion: 'Es evidente el cumplimiento de la estrategia y los indicadores confirman que las acciones tomadas han sido efectivas.' },
];
