import { empresas, auditorias, reportes, hallazgos, cuestionarios, preguntasPorCuestionario } from '../../data/mockData';
import { useAppStore, useProgresoCuestionario } from '../../store/useAppStore';

const HISTORICO = [
  { nombre: 'Generación\ny control', puntaje: 3.8, color: 'var(--brand-mid)' },
  { nombre: 'Indicadores\ndesempeño', puntaje: 2.1, color: '#E67E22' },
  { nombre: 'Costos de\nmantenimiento', puntaje: 4.2, color: '#27AE60' },
  { nombre: 'Flujo\nde caja', puntaje: 1.9, color: '#C0392B' },
  { nombre: 'Gestión\ncomercial', puntaje: 3.4, color: 'var(--brand)' },
];

const SEVERIDAD_COLOR: Record<string, string> = { critica: '#C0392B', alta: '#C0392B', media: '#D4860A', baja: '#27AE60' };

export function ReportesPage() {
  const { cuestionarioActivoId, mostrarToast } = useAppStore();
  const cuestionarioActivo = cuestionarios.find((c) => c.id === cuestionarioActivoId);
  const preguntaIds = cuestionarioActivo ? (preguntasPorCuestionario[cuestionarioActivo.id] ?? []).map((p) => p.id) : [];
  const progreso = useProgresoCuestionario(preguntaIds);
  const enVivo = progreso.respondidas > 0 && progreso.promedio !== null;

  const empresaPorId = Object.fromEntries(empresas.map((e) => [e.id, e]));

  const criticos = hallazgos.filter((h) => h.severidad === 'critica').length;
  const moderados = hallazgos.filter((h) => h.severidad === 'media').length;
  const leves = hallazgos.filter((h) => h.severidad === 'baja').length;

  return (
    <div>
      <div className="report-grid">
        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-hd">
            <div>
              <div className="card-title">Puntaje promedio por cuestionario</div>
              <div className="card-sub">{empresas[0].razonSocial} — sesión actual</div>
            </div>
            {enVivo && <span className="badge b-info"><i className="ti ti-live-view" style={{ fontSize: 11 }} /> En vivo</span>}
          </div>

          {enVivo && (
            <div style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 11, color: 'var(--ok)', fontWeight: 600, marginBottom: 6 }}>
                <i className="ti ti-circle-check" /> Cuestionario activo — puntaje calculado
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                <div style={{ flex: 1, background: 'var(--border-light)', borderRadius: 4, height: 10, overflow: 'hidden' }}>
                  <div style={{ height: '100%', borderRadius: 4, background: 'var(--brand-mid)', width: `${(progreso.promedio! / 5) * 100}%` }} />
                </div>
                <div style={{ fontSize: 14, fontWeight: 700, color: 'var(--brand)' }}>{progreso.promedio}</div>
              </div>
            </div>
          )}

          <div className="bar-chart">
            {HISTORICO.map((b) => (
              <div className="bar-col" key={b.nombre}>
                <div className="bar-v">{b.puntaje}</div>
                <div className="bar-fill" style={{ height: `${(b.puntaje / 5) * 100}px`, background: b.color }} />
                <div className="bar-nm" style={{ whiteSpace: 'pre-line' }}>{b.nombre}</div>
              </div>
            ))}
            {enVivo && (
              <div className="bar-col">
                <div className="bar-v">{progreso.promedio}</div>
                <div className="bar-fill" style={{ height: `${(progreso.promedio! / 5) * 100}px`, background: '#27AE60' }} />
                <div className="bar-nm">Sesión<br />actual</div>
              </div>
            )}
          </div>
        </div>

        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-hd">
            <div>
              <div className="card-title">Distribución de hallazgos</div>
              <div className="card-sub">{hallazgos.length} hallazgos totales detectados</div>
            </div>
          </div>
          <div className="donut-wrap">
            <svg width="110" height="110" viewBox="0 0 110 110" aria-hidden="true">
              <circle cx="55" cy="55" r="40" fill="none" stroke="#EEF1F5" strokeWidth="18" />
              <circle cx="55" cy="55" r="40" fill="none" stroke="#C0392B" strokeWidth="18" strokeDasharray="75 176" strokeDashoffset="-62" transform="rotate(-90 55 55)" />
              <circle cx="55" cy="55" r="40" fill="none" stroke="#D4860A" strokeWidth="18" strokeDasharray="100 151" strokeDashoffset="-137" transform="rotate(-90 55 55)" />
              <circle cx="55" cy="55" r="40" fill="none" stroke="#27AE60" strokeWidth="18" strokeDasharray="76 175" strokeDashoffset="-237" transform="rotate(-90 55 55)" />
              <text x="55" y="50" textAnchor="middle" fontSize="20" fontWeight="600" fill="#1A202C">{hallazgos.length}</text>
              <text x="55" y="65" textAnchor="middle" fontSize="10" fill="#8896A8">hallazgos</text>
            </svg>
            <div>
              <div className="leg-row"><div className="leg-dot" style={{ background: SEVERIDAD_COLOR.critica }} /> Crítico — {criticos}</div>
              <div className="leg-row"><div className="leg-dot" style={{ background: SEVERIDAD_COLOR.media }} /> Moderado — {moderados}</div>
              <div className="leg-row"><div className="leg-dot" style={{ background: SEVERIDAD_COLOR.baja }} /> Leve — {leves}</div>
            </div>
          </div>
          {enVivo && (
            <div style={{ marginTop: 14, paddingTop: 12, borderTop: '1px solid var(--border-light)' }}>
              <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--text-3)', textTransform: 'uppercase', letterSpacing: '.05em', marginBottom: 8 }}>Sesión actual</div>
              <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
                <div><div style={{ fontSize: 10, color: 'var(--text-3)' }}>Respondidas</div><div style={{ fontSize: 18, fontWeight: 700, color: 'var(--brand)' }}>{progreso.respondidas}</div></div>
                <div><div style={{ fontSize: 10, color: 'var(--text-3)' }}>Puntaje</div><div style={{ fontSize: 18, fontWeight: 700, color: 'var(--ok)' }}>{progreso.promedio}</div></div>
                <div><div style={{ fontSize: 10, color: 'var(--text-3)' }}>Nivel</div><div style={{ fontSize: 13, fontWeight: 600, marginTop: 3 }}>
                  {progreso.promedio! >= 4 ? '🟢 Avanzado' : progreso.promedio! >= 3 ? '🔵 Intermedio' : progreso.promedio! >= 2 ? '🟡 En desarrollo' : '🔴 Básico/reactivo'}
                </div></div>
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <div className="card-hd">
          <div>
            <div className="card-title">Historial de reportes</div>
            <div className="card-sub">Todas las auditorías completadas — el cliente puede acceder con su usuario</div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <button className="btn btn-sm"><i className="ti ti-download" /> Exportar PDF</button>
            <button className="btn btn-primary btn-sm" onClick={() => mostrarToast('Reporte enviado al cliente', 'ok')}>
              <i className="ti ti-send" /> Enviar al cliente
            </button>
          </div>
        </div>
        <div className="tbl-wrap">
          <table className="tbl">
            <thead><tr><th>Empresa</th><th>Fecha</th><th>Puntaje</th><th>Hallazgos</th><th>Estado</th><th /></tr></thead>
            <tbody>
              {reportes.map((r) => {
                const auditoria = auditorias.find((a) => a.id === r.auditoriaId);
                const empresa = auditoria ? empresaPorId[auditoria.empresaId] : undefined;
                const n = hallazgos.filter((h) => h.auditoriaId === r.auditoriaId).length;
                return (
                  <tr key={r.id}>
                    <td><strong>{empresa?.razonSocial}</strong></td>
                    <td>{r.generadoEn}</td>
                    <td><span className={`badge ${r.puntajeTotal! >= 3 ? 'b-ok' : r.puntajeTotal! >= 2 ? 'b-warn' : 'b-danger'}`}>{r.puntajeTotal?.toFixed(1)} / 5</span></td>
                    <td>{n}</td>
                    <td><span className="badge b-ok">Completada</span></td>
                    <td><div className="t-actions"><button className="btn btn-sm"><i className="ti ti-eye" /> Ver</button><button className="btn btn-sm"><i className="ti ti-download" /></button></div></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
