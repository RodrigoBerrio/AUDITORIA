import { Bar, BarChart, Cell, LabelList, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartContainer } from './ChartContainer';
import type { PuntajeCategoria } from '../../types/domain';

interface HistogramaCategoriasProps {
  categorias: PuntajeCategoria[];
}

interface DatoCategoria { categoria: string; puntaje: number; color: string }

/** Color por posición relativa entre las categorías mostradas (no por el semáforo absoluto de
 * madurez): la de menor puntaje en rojo, la de mayor en verde, y las intermedias en amarillo. */
function colorPorRanking(completas: PuntajeCategoria[]): Map<string, string> {
  const ordenadas = [...completas].sort((a, b) => a.puntaje! - b.puntaje!);
  const colores = new Map<string, string>();
  ordenadas.forEach((c, i) => {
    let color: string;
    if (ordenadas.length === 1) color = c.colorSemaforo; // sin nada que comparar, se usa el semáforo normal
    else if (i === 0) color = '#C0392B'; // más débil
    else if (i === ordenadas.length - 1) color = '#27AE60'; // mejor calificada
    else color = '#E4A317'; // intermedia
    colores.set(c.categoria, color);
  });
  return colores;
}

/** Parte una etiqueta larga en varias líneas (~14 caracteres) sin cortar palabras. */
function partirEnLineas(texto: string, maxCaracteres = 14): string[] {
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

/** Tick multilínea para el eje X — evita que nombres de categoría largos se corten o se encimen. */
function TickCategoria({ x, y, payload }: { x?: number | string; y?: number | string; payload?: { value: string } }) {
  if (!payload) return null;
  const lineas = partirEnLineas(payload.value);
  return (
    <text x={x} y={y} textAnchor="middle" fontSize={12} fill="var(--text-2)">
      {lineas.map((linea, i) => (
        <tspan key={i} x={x} dy={i === 0 ? 14 : 14}>{linea}</tspan>
      ))}
    </text>
  );
}

/** Etiqueta con el puntaje encima de cada barra. */
function EtiquetaBarra(props: { x?: number | string; y?: number | string; width?: number | string; index?: number; datos: DatoCategoria[] }) {
  const { x = 0, y = 0, width = 0, index = 0, datos } = props;
  const dato = datos[index];
  if (!dato) return null;
  const cx = Number(x) + Number(width) / 2;
  const cy = Number(y) - 8;
  return <text x={cx} y={cy} textAnchor="middle" fontSize={12} fontWeight={600} fill="var(--text-1)">{dato.puntaje.toFixed(1)}</text>;
}

/** Histograma general en barras verticales — una por categoría completa, coloreada con el mismo
 * semáforo que el resto del dashboard. Complementa al radar unificado (misma fuente de datos). */
export function HistogramaCategorias({ categorias }: HistogramaCategoriasProps) {
  const completas = categorias.filter((c) => c.completa && c.puntaje != null);
  const vacio = completas.length === 0;
  const colores = colorPorRanking(completas);
  const datos = completas.map((c) => ({ categoria: c.categoria, puntaje: c.puntaje!, color: colores.get(c.categoria)! }));

  const resumen = completas.map((c) => `${c.categoria}: ${c.puntaje!.toFixed(1)} / 5.0`).join('; ');

  return (
    <ChartContainer
      titulo="Histograma general por categoría"
      subtitulo="Rojo = la más débil · Amarillo = intermedia · Verde = la mejor calificada"
      ariaLabel={`Histograma general por categoría, escala 1 a 5. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Todavía no hay categorías completas para graficar."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {completas.map((c) => (
            <li key={c.categoria}>{c.categoria}: {c.puntaje!.toFixed(1)} / 5.0</li>
          ))}
        </ul>
      }
    >
      <ResponsiveContainer width="100%" height={520}>
        <BarChart data={datos} margin={{ top: 24, right: 16, bottom: 24, left: 0 }}>
          <XAxis dataKey="categoria" tick={(props) => <TickCategoria {...props} />} axisLine={false} tickLine={false} interval={0} height={60} />
          <YAxis domain={[0, 5]} tick={{ fontSize: 11, fill: 'var(--text-3)' }} axisLine={false} tickLine={false} />
          <Tooltip formatter={(valor) => Number(valor).toFixed(1)} />
          <Bar dataKey="puntaje" radius={[4, 4, 0, 0]} maxBarSize={90} isAnimationActive={false}>
            {datos.map((d) => <Cell key={d.categoria} fill={d.color} />)}
            <LabelList dataKey="puntaje" content={(props) => <EtiquetaBarra {...props} datos={datos} />} />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
