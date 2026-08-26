import {
  Legend, PolarAngleAxis, PolarGrid, PolarRadiusAxis, Radar, RadarChart, ResponsiveContainer, Tooltip,
} from 'recharts';
import { ChartContainer } from './ChartContainer';
import type { PuntajeSubcategoria } from '../../types/domain';

interface RadarMadurezProps {
  subcategorias: PuntajeSubcategoria[];
}

/** Parte una etiqueta larga en varias líneas (~13 caracteres) sin cortar palabras. */
function partirEnLineas(texto: string, maxCaracteres = 13): string[] {
  const palabras = texto.split(' ');
  const lineas: string[] = [];
  let actual = '';
  for (const palabra of palabras) {
    const candidato = actual ? `${actual} ${palabra}` : palabra;
    if (candidato.length > maxCaracteres && actual) {
      lineas.push(actual);
      actual = palabra;
    } else {
      actual = candidato;
    }
  }
  if (actual) lineas.push(actual);
  return lineas;
}

interface TickSubcategoriaProps {
  x?: number | string;
  y?: number | string;
  payload?: { value: string };
  textAnchor?: 'inherit' | 'start' | 'middle' | 'end';
}

/** Tick multilínea para el eje del radar — evita que los nombres de subcategoría largos
 * ("Habilidades del personal de mantenimiento") se corten contra el borde del gráfico. */
function TickSubcategoria({ x, y, payload, textAnchor }: TickSubcategoriaProps) {
  if (!payload) return null;
  const lineas = partirEnLineas(payload.value);
  const offsetInicial = -((lineas.length - 1) * 5.5);
  return (
    <text x={x} y={y} textAnchor={textAnchor} fontSize={10.5} fill="var(--text-2)">
      {lineas.map((linea, i) => (
        <tspan key={i} x={x} dy={i === 0 ? offsetInicial : 11}>{linea}</tspan>
      ))}
    </text>
  );
}

/** Un eje por subcategoría aplicada (3 a 10 sin romper el layout): "Puntaje actual" sólido vs. "Meta" punteada sin relleno. */
export function RadarMadurez({ subcategorias }: RadarMadurezProps) {
  const vacio = subcategorias.length === 0;
  const datos = subcategorias.map((s) => ({
    subcategoria: s.subcategoria,
    actual: s.puntaje ?? 0,
    meta: s.meta,
  }));

  const resumen = subcategorias
    .map((s) => `${s.subcategoria}: ${s.evaluada && s.puntaje != null ? s.puntaje.toFixed(1) : 'pendiente de evaluar'} de meta ${s.meta.toFixed(1)}`)
    .join('; ');

  return (
    <ChartContainer
      titulo="Radar de madurez por subcategoría"
      subtitulo="Puntaje actual vs. meta configurable"
      ariaLabel={`Radar de madurez por subcategoría, escala 1 a 5. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Esta auditoría todavía no tiene cuestionarios aplicados."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {subcategorias.map((s) => (
            <li key={s.subcategoria}>
              {s.subcategoria}: {s.evaluada && s.puntaje != null ? `${s.puntaje.toFixed(1)} / 5.0` : 'pendiente de evaluar'} (meta {s.meta.toFixed(1)})
            </li>
          ))}
        </ul>
      }
    >
      <ResponsiveContainer width="100%" height={Math.max(300, datos.length * 46)}>
        <RadarChart data={datos} outerRadius="50%" margin={{ top: 16, right: 8, bottom: 16, left: 8 }}>
          <PolarGrid stroke="var(--border)" />
          <PolarAngleAxis dataKey="subcategoria" tick={(props) => <TickSubcategoria {...props} />} />
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
