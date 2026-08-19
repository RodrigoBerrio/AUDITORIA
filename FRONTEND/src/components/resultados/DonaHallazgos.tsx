import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { ChartContainer } from './ChartContainer';
import { ETIQUETA_SEVERIDAD } from '../../config/semaforo';
import type { ConteoSeveridad } from '../../types/domain';

interface DonaHallazgosProps {
  total: number;
  porSeveridad: ConteoSeveridad[];
}

/** Dona (nunca torta) de hallazgos por severidad; leyenda con conteo y porcentaje, no solo color (regla de accesibilidad del spec). */
export function DonaHallazgos({ total, porSeveridad }: DonaHallazgosProps) {
  const vacio = total === 0;
  const datos = porSeveridad.filter((c) => c.cantidad > 0);

  const resumen = datos
    .map((c) => `${ETIQUETA_SEVERIDAD[c.severidad] ?? c.severidad}: ${c.cantidad} (${c.porcentaje.toFixed(1)}%)`)
    .join('; ');

  return (
    <ChartContainer
      titulo="Hallazgos por severidad"
      subtitulo={`${total} hallazgo${total === 1 ? '' : 's'} totales detectados`}
      ariaLabel={`Distribución de ${total} hallazgos por severidad. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Esta auditoría todavía no tiene hallazgos registrados."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {datos.map((c) => (
            <li key={c.severidad}>{ETIQUETA_SEVERIDAD[c.severidad] ?? c.severidad}: {c.cantidad} ({c.porcentaje.toFixed(1)}%)</li>
          ))}
        </ul>
      }
    >
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie data={datos} dataKey="cantidad" nameKey="severidad" innerRadius="55%" outerRadius="80%" paddingAngle={2} isAnimationActive={false}>
            {datos.map((c) => (
              <Cell key={c.severidad} fill={c.colorHex} stroke="var(--surface)" strokeWidth={2} />
            ))}
          </Pie>
          <Tooltip
            formatter={(valor, _nombre, item) => {
              const payload = (item as unknown as { payload: ConteoSeveridad }).payload;
              return [`${valor} (${payload.porcentaje.toFixed(1)}%)`, ETIQUETA_SEVERIDAD[payload.severidad] ?? payload.severidad];
            }}
          />
          <Legend
            formatter={(_valor, entry) => {
              const payload = (entry as unknown as { payload: ConteoSeveridad }).payload;
              return `${ETIQUETA_SEVERIDAD[payload.severidad] ?? payload.severidad} — ${payload.cantidad} (${payload.porcentaje.toFixed(1)}%)`;
            }}
            wrapperStyle={{ fontSize: 12 }}
          />
        </PieChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
