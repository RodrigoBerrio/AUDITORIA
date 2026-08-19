import { CartesianGrid, Line, LineChart, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { ChartContainer } from './ChartContainer';
import type { HistoricoPunto } from '../../types/domain';

interface TendenciaHistoricaProps {
  historico: HistoricoPunto[];
  meta?: number;
}

/** Línea de tendencia de puntaje_global entre auditorías sucesivas de la misma empresa; solo tiene sentido con 2+ puntos. */
export function TendenciaHistorica({ historico, meta = 4.0 }: TendenciaHistoricaProps) {
  if (historico.length < 2) {
    return null;
  }

  const datos = historico.map((h) => ({ fecha: h.fechaFin, puntaje: h.puntajeGlobal ?? 0 }));
  const resumen = datos.map((d) => `${d.fecha}: ${d.puntaje.toFixed(1)}`).join('; ');

  return (
    <ChartContainer
      titulo="Tendencia histórica"
      subtitulo="Puntaje global entre auditorías sucesivas de esta empresa"
      ariaLabel={`Tendencia de puntaje global a lo largo del tiempo. ${resumen}. Meta de referencia ${meta.toFixed(1)}.`}
      fallbackTexto={
        <ol style={{ paddingLeft: 18 }}>
          {datos.map((d) => (
            <li key={d.fecha}>{d.fecha}: {d.puntaje.toFixed(1)} / 5.0</li>
          ))}
        </ol>
      }
    >
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={datos} margin={{ top: 10, right: 20, bottom: 4, left: 4 }}>
          <CartesianGrid stroke="var(--border-light)" vertical={false} />
          <XAxis dataKey="fecha" tick={{ fontSize: 11, fill: 'var(--text-3)' }} axisLine={false} tickLine={false} />
          <YAxis domain={[0, 5]} tick={{ fontSize: 11, fill: 'var(--text-3)' }} axisLine={false} tickLine={false} />
          <Tooltip formatter={(valor) => Number(valor).toFixed(1)} />
          <ReferenceLine y={meta} stroke="var(--ok)" strokeDasharray="5 4" label={{ value: `Meta ${meta.toFixed(1)}`, fontSize: 10, fill: 'var(--ok)', position: 'insideTopLeft' }} />
          <Line type="monotone" dataKey="puntaje" stroke="var(--brand-mid)" strokeWidth={2} dot={{ r: 4, fill: 'var(--brand-mid)' }} isAnimationActive={false} />
        </LineChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
