import type { ReactNode } from 'react';
import { Bar, BarChart, Cell, LabelList, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartContainer } from './ChartContainer';
import type { ItemPuntaje } from '../../types/domain';

interface RankingBarrasProps {
  titulo: string;
  subtitulo?: string;
  items: ItemPuntaje[];
  mensajeVacio?: string;
  headerExtra?: ReactNode;
}

interface DatoBarra extends ItemPuntaje {
  valor: number;
}

/** Etiqueta a la derecha de cada barra: el puntaje real, o "Pendiente" en gris para las filas todavía sin evaluar. */
function EtiquetaBarra(props: { x?: number | string; y?: number | string; width?: number | string; height?: number | string; index?: number; datos: DatoBarra[] }) {
  const { x = 0, y = 0, width = 0, height = 0, index = 0, datos } = props;
  const item = datos[index];
  if (!item) return null;
  const cx = Number(x) + Number(width) + 8;
  const cy = Number(y) + Number(height) / 2 + 4;
  return item.evaluada
    ? <text x={cx} y={cy} fontSize={12} fontWeight={600} fill="var(--text-1)">{item.puntaje!.toFixed(1)}</text>
    : <text x={cx} y={cy} fontSize={11.5} fontStyle="italic" fill="var(--text-3)">Pendiente</text>;
}

/**
 * Barras horizontales rankeadas (menor a mayor puntaje, ya viene ordenado del backend); mismo
 * componente para subcategorías, categorías y secciones. Las filas con evaluada=false (todavía
 * sin completar) se dibujan como un marco punteado vacío que recorre toda la escala 0-5 — el
 * "plano cartesiano vacío" — en vez de una barra de color en cero, que se leería como un puntaje
 * real malo.
 */
export function RankingBarras({ titulo, subtitulo, items, mensajeVacio, headerExtra }: RankingBarrasProps) {
  const vacio = items.length === 0;
  const datos: DatoBarra[] = items.map((i) => ({ ...i, valor: i.evaluada ? (i.puntaje ?? 0) : 5 }));

  const resumen = items
    .map((i) => `${i.etiqueta}: ${i.evaluada && i.puntaje != null ? i.puntaje.toFixed(1) : 'pendiente de evaluar'}`)
    .join('; ');

  return (
    <ChartContainer
      titulo={titulo}
      subtitulo={subtitulo}
      ariaLabel={`${titulo}, escala 1 a 5, de menor a mayor puntaje. ${resumen}`}
      vacio={vacio}
      mensajeVacio={mensajeVacio}
      headerExtra={headerExtra}
      fallbackTexto={
        <ol style={{ paddingLeft: 18 }}>
          {items.map((i) => (
            <li key={i.etiqueta}>{i.etiqueta}: {i.evaluada && i.puntaje != null ? `${i.puntaje.toFixed(1)} / 5.0` : 'pendiente de evaluar'}</li>
          ))}
        </ol>
      }
    >
      <ResponsiveContainer width="100%" height={Math.max(120, datos.length * 60 + 20)}>
        <BarChart data={datos} layout="vertical" margin={{ top: 4, right: 68, bottom: 4, left: 4 }}>
          <XAxis type="number" domain={[0, 5]} tick={{ fontSize: 11, fill: 'var(--text-3)' }} axisLine={false} tickLine={false} />
          <YAxis
            type="category"
            dataKey="etiqueta"
            width={165}
            tick={{ fontSize: 11.5, fill: 'var(--text-1)' }}
            axisLine={false}
            tickLine={false}
          />
          <Tooltip
            formatter={(valor, _nombre, item) => {
              const dato = (item as unknown as { payload: DatoBarra }).payload;
              return dato.evaluada ? Number(valor).toFixed(1) : 'Pendiente de evaluar';
            }}
            cursor={{ fill: 'var(--surface-2)' }}
          />
          <Bar dataKey="valor" radius={[0, 4, 4, 0]} maxBarSize={22} isAnimationActive={false}>
            {datos.map((d) => (
              <Cell
                key={d.etiqueta}
                fill={d.evaluada ? d.colorSemaforo : 'transparent'}
                stroke={d.evaluada ? d.colorSemaforo : 'var(--border)'}
                strokeWidth={d.evaluada ? 0 : 1.5}
                strokeDasharray={d.evaluada ? undefined : '4 3'}
              />
            ))}
            <LabelList content={(props) => <EtiquetaBarra {...props} datos={datos} />} />
          </Bar>
        </BarChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
