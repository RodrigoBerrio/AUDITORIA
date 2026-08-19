import { ChartContainer } from './ChartContainer';
import { ETIQUETA_ESTADO_HALLAZGO } from '../../config/semaforo';
import type { ConteoEstado } from '../../types/domain';

interface AvanceHallazgosProps {
  total: number;
  porEstado: ConteoEstado[];
}

const COLOR_ESTADO: Record<string, string> = {
  abierto: '#C0392B',
  en_tratamiento: '#D4860A',
  cerrado: '#27AE60',
};

/** Barra apilada horizontal de avance del plan de acción (hallazgo.estado); alternativa a gráfico de progreso, con leyenda numérica siempre visible. */
export function AvanceHallazgos({ total, porEstado }: AvanceHallazgosProps) {
  const vacio = total === 0;
  const resumen = porEstado
    .map((c) => `${ETIQUETA_ESTADO_HALLAZGO[c.estado] ?? c.estado}: ${c.cantidad} (${c.porcentaje.toFixed(1)}%)`)
    .join('; ');

  return (
    <ChartContainer
      titulo="Avance del plan de acción"
      subtitulo="Hallazgos agrupados por estado de tratamiento"
      ariaLabel={`Avance del plan de acción sobre ${total} hallazgos. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Esta auditoría todavía no tiene hallazgos registrados."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {porEstado.map((c) => (
            <li key={c.estado}>{ETIQUETA_ESTADO_HALLAZGO[c.estado] ?? c.estado}: {c.cantidad} ({c.porcentaje.toFixed(1)}%)</li>
          ))}
        </ul>
      }
    >
      <div style={{ display: 'flex', height: 22, borderRadius: 6, overflow: 'hidden', background: 'var(--border-light)', gap: 2 }}>
        {porEstado.filter((c) => c.cantidad > 0).map((c) => (
          <div
            key={c.estado}
            style={{ width: `${c.porcentaje}%`, minWidth: c.cantidad > 0 ? 6 : 0, background: COLOR_ESTADO[c.estado] ?? 'var(--text-3)' }}
            title={`${ETIQUETA_ESTADO_HALLAZGO[c.estado] ?? c.estado}: ${c.cantidad} (${c.porcentaje.toFixed(1)}%)`}
          />
        ))}
      </div>

      <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', marginTop: 14 }}>
        {porEstado.map((c) => (
          <div className="leg-row" key={c.estado} style={{ marginBottom: 0 }}>
            <div className="leg-dot" style={{ background: COLOR_ESTADO[c.estado] ?? 'var(--text-3)' }} />
            {ETIQUETA_ESTADO_HALLAZGO[c.estado] ?? c.estado} — {c.cantidad} ({c.porcentaje.toFixed(1)}%)
          </div>
        ))}
      </div>
    </ChartContainer>
  );
}
