// ============================================================
// Escala de 4 niveles del desglose por criterio/dimensión dentro de una
// subcategoría (CategoriasProgreso): crítico/serio/alerta/bueno con cortes
// en 2, 3 y 4 — distinta del semáforo único de 3 niveles (KPI/radar/ranking/
// dona a nivel de subcategoría/categoría/auditoría, cortes en 2.5/3.75, que
// sigue intacto). El color en sí ya lo calcula el backend (SemaforoDetalle.java,
// mismos hex) y viaja en `colorSemaforo` de cada ItemPuntaje — este archivo
// solo describe la escala para la leyenda visual, no recalcula nada.
// ============================================================

export interface NivelDetalle {
  etiqueta: string;
  rango: string;
  color: string;
}

export const NIVELES_DETALLE: NivelDetalle[] = [
  { etiqueta: 'Crítico', rango: '<2', color: '#C0392B' },
  { etiqueta: 'Serio', rango: '2–3', color: '#E0805C' },
  { etiqueta: 'Alerta', rango: '3–4', color: '#E4A317' },
  { etiqueta: 'Bueno', rango: '≥4', color: '#2E9E5B' },
];
