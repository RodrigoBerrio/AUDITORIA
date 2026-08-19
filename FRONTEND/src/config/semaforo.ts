// ============================================================
// Semáforo único de la app (regla no negociable del spec de
// resultados gráficos): mismos umbrales y colores en KPI, radar,
// ranking, dona y PDF. Debe mantenerse sincronizado a mano con
// Semaforo.java (backend) — los mismos hex ya se usaban en
// theme.css / ReportesPage.tsx antes de este módulo.
//
// Los componentes de resultados SIEMPRE prefieren el campo
// `colorSemaforo` que ya viene calculado en la respuesta de la
// API (una sola fuente de verdad, por eso el backend lo calcula).
// Este archivo cubre los casos sin round-trip: leyendas estáticas,
// badges sueltos de severidad, o un fallback si la API aún no
// respondió.
// ============================================================

export const UMBRAL_CRITICO = 2.5;
export const UMBRAL_CONSOLIDADO = 3.75;

export const COLOR_CRITICO = '#C0392B';
export const COLOR_EN_DESARROLLO = '#D4860A';
export const COLOR_CONSOLIDADO = '#27AE60';
/** Sin puntaje todavía — gris neutro, nunca rojo: "sin medir" no es "crítico". Mismo tono que --text-3. */
export const COLOR_SIN_DATOS = '#8896A8';

export function colorPorPuntaje(puntaje: number | null | undefined): string {
  if (puntaje == null) return COLOR_SIN_DATOS;
  if (puntaje < UMBRAL_CRITICO) return COLOR_CRITICO;
  if (puntaje < UMBRAL_CONSOLIDADO) return COLOR_EN_DESARROLLO;
  return COLOR_CONSOLIDADO;
}

/**
 * Paleta de severidad de 4 pasos verde-amarillo-naranja-rojo; mismos hex que
 * Severidad.colorHex() en el backend. MEDIA y ALTA solían ser dos naranjas
 * casi idénticos (#D4860A/#E67E22) — poco distinguibles en la dona de
 * hallazgos — ahora tienen matices bien separados.
 */
export const COLOR_SEVERIDAD: Record<string, string> = {
  baja: '#2E9E5B',
  media: '#E4A317',
  alta: '#D9541C',
  critica: '#B0281A',
};

export const ETIQUETA_SEVERIDAD: Record<string, string> = {
  baja: 'Baja',
  media: 'Media',
  alta: 'Alta',
  critica: 'Crítica',
};

export const ETIQUETA_ESTADO_HALLAZGO: Record<string, string> = {
  abierto: 'Abierto',
  en_tratamiento: 'En tratamiento',
  cerrado: 'Cerrado',
};
