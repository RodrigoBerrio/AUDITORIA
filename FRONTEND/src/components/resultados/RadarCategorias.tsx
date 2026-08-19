import {
  Legend, PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, Tooltip,
} from 'recharts';
import { ChartContainer } from './ChartContainer';
import type { PuntajeCategoria } from '../../types/domain';

interface RadarCategoriasProps {
  categorias: PuntajeCategoria[];
  meta?: number;
}

/**
 * Radar general a nivel catálogo: un eje por CATEGORÍA, nunca por subcategoría mezclada entre
 * categorías distintas (error corregido del spec — cada categoría tiene su propio radar de
 * subcategorías en CategoriasProgreso; este es el único que combina categorías, y solo se muestra
 * una vez que todas están completas).
 */
export function RadarCategorias({ categorias, meta = 4.0 }: RadarCategoriasProps) {
  const vacio = categorias.length === 0;
  const datos = categorias.map((c) => ({
    categoria: c.categoria,
    actual: c.puntaje ?? 0,
    meta,
  }));

  const resumen = categorias
    .map((c) => `${c.categoria}: ${c.puntaje != null ? c.puntaje.toFixed(1) : 'sin datos'} de meta ${meta.toFixed(1)}`)
    .join('; ');

  return (
    <ChartContainer
      titulo="Radar general por categoría"
      subtitulo="Auditoría integral — puntaje promedio de cada categoría vs. meta"
      ariaLabel={`Radar general por categoría, escala 1 a 5. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Todavía no hay categorías completas."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {categorias.map((c) => (
            <li key={c.categoria}>
              {c.categoria}: {c.puntaje != null ? `${c.puntaje.toFixed(1)} / 5.0` : 'sin datos'} (meta {meta.toFixed(1)})
            </li>
          ))}
        </ul>
      }
    >
      <ResponsiveContainer width="100%" height={Math.max(280, datos.length * 40)}>
        <RadarChart data={datos} outerRadius="70%">
          <PolarGrid stroke="var(--border)" />
          <PolarAngleAxis dataKey="categoria" tick={{ fontSize: 12, fill: 'var(--text-2)' }} />
          <PolarRadiusAxis angle={90} domain={[0, 5]} tick={{ fontSize: 10, fill: 'var(--text-3)' }} tickCount={6} />
          <Radar name="Puntaje actual" dataKey="actual" stroke="var(--brand-mid)" fill="var(--brand-mid)" fillOpacity={0.35} strokeWidth={2} />
          <Radar name="Meta" dataKey="meta" stroke="var(--ok)" fill="none" strokeWidth={1.5} strokeDasharray="5 4" />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Tooltip formatter={(valor) => Number(valor).toFixed(1)} />
        </RadarChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
