import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
import { api } from '../../api/client';
import type { Auditoria, Empresa } from '../../types/domain';

const ESTADO_BADGE: Record<string, { clase: string; icono: string; texto: string }> = {
  finalizada: { clase: 'b-ok', icono: 'ti-check', texto: 'Completada' },
  en_progreso: { clase: 'b-warn', icono: 'ti-clock', texto: 'En progreso' },
  cancelada: { clase: 'b-gray', icono: 'ti-x', texto: 'Cancelada' },
};

export function DashboardPage() {
  const accessToken = useAppStore((s) => s.accessToken);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [auditorias, setAuditorias] = useState<Auditoria[]>([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    setCargando(true);
    Promise.all([
      api.get<Empresa[]>('/api/empresas', accessToken),
      api.get<Auditoria[]>('/api/auditorias', accessToken),
    ])
      .then(([empresasRes, auditoriasRes]) => {
        setEmpresas(empresasRes);
        setAuditorias(auditoriasRes);
      })
      .finally(() => setCargando(false));
  }, [accessToken]);

  const completadas = auditorias.filter((a) => a.estado === 'finalizada').length;
  const enProgreso = auditorias.filter((a) => a.estado === 'en_progreso').length;
  // JSON deserializa los BigDecimal nulos del backend como `null`, no `undefined` — hay que cubrir ambos.
  const puntajes = auditorias.filter((a) => a.puntajeGlobal != null).map((a) => a.puntajeGlobal!);
  const promedio = puntajes.length ? (puntajes.reduce((s, v) => s + v, 0) / puntajes.length).toFixed(1) : '—';

  const empresaPorId = Object.fromEntries(empresas.map((e) => [e.id, e]));

  return (
    <div>
      <div className="metrics">
        <div className="metric">
          <div className="metric-icon"><i className="ti ti-building" /></div>
          <div className="metric-value">{empresas.length}</div>
          <div className="metric-label">Empresas registradas</div>
          <div className="metric-delta delta-up"><i className="ti ti-trending-up" style={{ fontSize: 12 }} /> sesión activa</div>
        </div>
        <div className="metric t-ok">
          <div className="metric-icon"><i className="ti ti-clipboard-check" /></div>
          <div className="metric-value">{completadas}</div>
          <div className="metric-label">Auditorías completadas</div>
          <div className="metric-delta" style={{ color: 'var(--ok)' }}>{enProgreso} en progreso</div>
        </div>
        <div className="metric t-warn">
          <div className="metric-icon"><i className="ti ti-alert-triangle" /></div>
          <div className="metric-value">{puntajes.length}</div>
          <div className="metric-label">Cuestionarios con puntaje</div>
          <div className="metric-delta" style={{ color: 'var(--accent)' }}>Puntaje promedio: {promedio}</div>
        </div>
        <div className="metric">
          <div className="metric-icon"><i className="ti ti-chart-bar" /></div>
          <div className="metric-value">{completadas}</div>
          <div className="metric-label">Reportes generados</div>
          <div className="metric-delta">Último: {auditorias[0]?.fechaFin ?? '—'}</div>
        </div>
      </div>

      <div className="card">
        <div className="card-hd">
          <div>
            <div className="card-title">Auditorías recientes</div>
            <div className="card-sub">Estado de la última visita por empresa</div>
          </div>
          <Link className="btn btn-sm" to="/auditor/reportes"><i className="ti ti-eye" /> Ver reportes</Link>
        </div>
        <div className="tbl-wrap">
          <table className="tbl">
            <thead>
              <tr><th>Empresa</th><th>Fecha</th><th>Puntaje</th><th>Estado</th><th /></tr>
            </thead>
            <tbody>
              {cargando && (
                <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-3)' }}>Cargando auditorías…</td></tr>
              )}
              {!cargando && auditorias.length === 0 && (
                <tr><td colSpan={5} style={{ textAlign: 'center', color: 'var(--text-3)' }}>Todavía no hay auditorías registradas.</td></tr>
              )}
              {auditorias.map((a) => {
                const empresa = empresaPorId[a.empresaId];
                const badge = ESTADO_BADGE[a.estado];
                return (
                  <tr key={a.id}>
                    <td><strong>{empresa?.razonSocial}</strong></td>
                    <td>{a.fechaFin ?? a.fechaInicio}</td>
                    <td>{a.puntajeGlobal ? <strong>{a.puntajeGlobal.toFixed(1)} / 5</strong> : '—'}</td>
                    <td><span className={`badge ${badge.clase}`}><i className={`ti ${badge.icono}`} style={{ fontSize: 11 }} /> {badge.texto}</span></td>
                    <td>
                      <div className="t-actions">
                        {a.estado === 'en_progreso' ? (
                          <Link className="btn btn-primary btn-sm" to="/auditor/formulario"><i className="ti ti-pencil" /> Continuar</Link>
                        ) : (
                          <Link className="btn btn-sm" to="/auditor/reportes"><i className="ti ti-chart-bar" /> Reporte</Link>
                        )}
                      </div>
                    </td>
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
