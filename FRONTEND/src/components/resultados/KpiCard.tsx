interface KpiCardProps {
  puntajeGlobal: number | null;
  nivelMadurez: string | null;
  colorSemaforo: string;
  preguntasRespondidas: number;
  preguntasEsperadas: number;
}

/** Tarjeta KPI: puntaje_global en grande + badge de nivel_madurez con el color de semáforo que ya calculó el backend. */
export function KpiCard({ puntajeGlobal, nivelMadurez, colorSemaforo, preguntasRespondidas, preguntasEsperadas }: KpiCardProps) {
  const incompleta = preguntasEsperadas > 0 && preguntasRespondidas < preguntasEsperadas;
  const texto = puntajeGlobal != null ? `${puntajeGlobal.toFixed(1)} / 5.0` : 'Sin datos';

  return (
    <div
      className="card"
      style={{ marginBottom: 0 }}
      role="img"
      aria-label={`Puntaje global de la auditoría: ${texto}, nivel de madurez ${nivelMadurez ?? 'sin calcular'}`}
    >
      <div className="card-hd">
        <div>
          <div className="card-title">Puntaje global</div>
          <div className="card-sub">Promedio de todos los cuestionarios aplicados</div>
        </div>
      </div>

      <div style={{ fontSize: 40, fontWeight: 700, color: 'var(--text-1)', lineHeight: 1 }}>{texto}</div>

      {nivelMadurez && (
        <span className="badge" style={{ marginTop: 12, background: `${colorSemaforo}1A`, color: colorSemaforo }}>
          <i className="ti ti-shield-check" style={{ fontSize: 12 }} /> {nivelMadurez}
        </span>
      )}

      {incompleta && (
        <div className="hint" style={{ marginTop: 12 }}>
          <i className="ti ti-alert-triangle" style={{ color: 'var(--accent)' }} />
          Auditoría incompleta: {preguntasRespondidas} de {preguntasEsperadas} preguntas respondidas — el promedio puede no ser representativo.
        </div>
      )}
    </div>
  );
}
