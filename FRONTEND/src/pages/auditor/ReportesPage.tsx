import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client';
import { useAppStore } from '../../store/useAppStore';
import type { Auditoria, Empresa } from '../../types/domain';

interface FilaHistorial {
  auditoria: Auditoria;
  empresa?: Empresa;
  hallazgos: number;
}

/**
 * Historial de auditorías finalizadas — datos reales (GET /api/auditorias +
 * /api/empresas + /api/auditorias/:id/hallazgos). El detalle de puntaje,
 * gráficas y el botón de generar PDF viven todos en un solo lugar
 * (/auditor/auditorias/:id/resultados) para no duplicar esa lógica aquí.
 */
export function ReportesPage() {
  const accessToken = useAppStore((s) => s.accessToken);
  const [filas, setFilas] = useState<FilaHistorial[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    setCargando(true);
    Promise.all([
      api.get<Auditoria[]>('/api/auditorias', accessToken),
      api.get<Empresa[]>('/api/empresas', accessToken),
    ])
      .then(async ([auditorias, empresas]) => {
        const empresaPorId = Object.fromEntries(empresas.map((e) => [e.id, e]));
        const finalizadas = auditorias.filter((a) => a.estado === 'finalizada');
        const conHallazgos = await Promise.all(
          finalizadas.map(async (auditoria) => ({
            auditoria,
            empresa: empresaPorId[auditoria.empresaId],
            hallazgos: (await api.get<unknown[]>(`/api/auditorias/${auditoria.id}/hallazgos`, accessToken)).length,
          })),
        );
        conHallazgos.sort((a, b) => (b.auditoria.fechaFin ?? '').localeCompare(a.auditoria.fechaFin ?? ''));
        setFilas(conHallazgos);
      })
      .finally(() => setCargando(false));
  }, [accessToken]);

  const puntajes = filas.filter((f) => f.auditoria.puntajeGlobal != null).map((f) => f.auditoria.puntajeGlobal!);
  const promedio = puntajes.length ? (puntajes.reduce((s, v) => s + v, 0) / puntajes.length).toFixed(1) : '—';
  const totalHallazgos = filas.reduce((s, f) => s + f.hallazgos, 0);

  return (
    <div>
      <div className="metrics">
        <div className="metric t-ok">
          <div className="metric-icon"><i className="ti ti-clipboard-check" /></div>
          <div className="metric-value">{filas.length}</div>
          <div className="metric-label">Auditorías finalizadas</div>
        </div>
        <div className="metric">
          <div className="metric-icon"><i className="ti ti-chart-line" /></div>
          <div className="metric-value">{promedio}</div>
          <div className="metric-label">Puntaje promedio global</div>
        </div>
        <div className="metric t-warn">
          <div className="metric-icon"><i className="ti ti-alert-triangle" /></div>
          <div className="metric-value">{totalHallazgos}</div>
          <div className="metric-label">Hallazgos registrados</div>
        </div>
      </div>

      <div className="card">
        <div className="card-hd">
          <div>
            <div className="card-title">Historial de auditorías</div>
            <div className="card-sub">Auditorías finalizadas — entra a "Resultados" para ver gráficas y generar el PDF</div>
          </div>
        </div>
        <div className="tbl-wrap">
          <table className="tbl">
            <thead><tr><th>Empresa</th><th>Fecha</th><th>Puntaje</th><th>Hallazgos</th><th>Estado</th><th /></tr></thead>
            <tbody>
              {cargando && (
                <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-3)' }}>Cargando auditorías…</td></tr>
              )}
              {!cargando && filas.length === 0 && (
                <tr><td colSpan={6} style={{ textAlign: 'center', color: 'var(--text-3)' }}>Todavía no hay auditorías finalizadas.</td></tr>
              )}
              {filas.map(({ auditoria, empresa, hallazgos }) => (
                <tr key={auditoria.id}>
                  <td><strong>{empresa?.razonSocial ?? '—'}</strong></td>
                  <td>{auditoria.fechaFin}</td>
                  <td>
                    {auditoria.puntajeGlobal != null
                      ? <span className={`badge ${auditoria.puntajeGlobal >= 3.75 ? 'b-ok' : auditoria.puntajeGlobal >= 2.5 ? 'b-warn' : 'b-danger'}`}>{auditoria.puntajeGlobal.toFixed(1)} / 5</span>
                      : '—'}
                  </td>
                  <td>{hallazgos}</td>
                  <td><span className="badge b-ok">Completada</span></td>
                  <td>
                    <div className="t-actions">
                      <Link className="btn btn-primary btn-sm" to={`/auditor/auditorias/${auditoria.id}/resultados`}>
                        <i className="ti ti-chart-bar" /> Resultados
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
