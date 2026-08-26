import {
  Legend, PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, Tooltip,
} from 'recharts';
import { ChartContainer } from './ChartContainer';
import { TickEtiquetaLarga } from './radarTick';
import type { PuntajeCategoria } from '../../types/domain';

interface RadarCategoriasProps {
  categorias: PuntajeCategoria[];
  meta?: number;
}

/** Radar unificado con un eje por categoría (Mantenimiento, Diagnóstico energético, Sostenibilidad
 * energética, etc.) — solo sus puntajes globales, nunca mezclado con el detalle de subcategorías. */
export function RadarCategorias({ categorias, meta = 4.0 }: RadarCategoriasProps) {
  // Solo categorías completas: una incompleta con puntaje=null graficada en 0 se leería como un
  // mal resultado en vez de "todavía sin terminar" (regla "no evaluado ≠ 0").
  const completas = categorias.filter((c) => c.completa && c.puntaje != null);
  const vacio = completas.length === 0;
  const datos = completas.map((c) => ({
    categoria: c.categoria,
    actual: c.puntaje!,
    meta,
  }));

  const resumen = completas
    .map((c) => `${c.categoria}: ${c.puntaje!.toFixed(1)} de meta ${meta.toFixed(1)}`)
    .join('; ');

  return (
    <ChartContainer
      titulo="Radar unificado por categoría"
      subtitulo="Calificación global de cada categoría vs. meta configurable"
      ariaLabel={`Radar unificado por categoría, escala 1 a 5. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Todavía no hay categorías completas para graficar."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {completas.map((c) => (
            <li key={c.categoria}>{c.categoria}: {c.puntaje!.toFixed(1)} / 5.0 (meta {meta.toFixed(1)})</li>
          ))}
        </ul>
      }
    >
      <ResponsiveContainer width="100%" height={520}>
        <RadarChart data={datos} outerRadius="60%" margin={{ top: 24, right: 24, bottom: 24, left: 24 }}>
          <PolarGrid stroke="var(--border)" />
          <PolarAngleAxis dataKey="categoria" tick={(props) => <TickEtiquetaLarga {...props} />} />
          <PolarRadiusAxis angle={90} domain={[0, 5]} tick={{ fontSize: 10, fill: 'var(--text-3)' }} tickCount={6} />
          <Radar name="Puntaje actual" dataKey="actual" stroke="var(--brand-mid)" fill="var(--brand-mid)" fillOpacity={0.35} strokeWidth={2} isAnimationActive={false} />
          <Radar name="Meta" dataKey="meta" stroke="var(--ok)" fill="none" strokeWidth={1.5} strokeDasharray="5 4" isAnimationActive={false} />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Tooltip formatter={(valor) => Number(valor).toFixed(1)} />
        </RadarChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
