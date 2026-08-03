import { Link } from 'react-router-dom';

export function ClienteDashboardPage() {
  return (
    <div>
      <div className="metrics">
        <div className="metric t-ok">
          <div className="metric-icon"><i className="ti ti-clipboard-check" /></div>
          <div className="metric-value">4</div>
          <div className="metric-label">Auditorías recibidas</div>
          <div className="metric-delta delta-up">3 completadas</div>
        </div>
        <div className="metric">
          <div className="metric-icon"><i className="ti ti-chart-line" /></div>
          <div className="metric-value">3.8</div>
          <div className="metric-label">Puntaje promedio</div>
          <div className="metric-delta" style={{ color: 'var(--ok)' }}>Escala 1 – 5</div>
        </div>
        <div className="metric t-warn">
          <div className="metric-icon"><i className="ti ti-alert-triangle" /></div>
          <div className="metric-value">8</div>
          <div className="metric-label">Hallazgos activos</div>
          <div className="metric-delta" style={{ color: 'var(--accent)' }}>3 críticos</div>
        </div>
        <div className="metric">
          <div className="metric-icon"><i className="ti ti-calendar" /></div>
          <div className="metric-value">Jul</div>
          <div className="metric-label">Próxima auditoría</div>
          <div className="metric-delta">Estimada 2026</div>
        </div>
      </div>

      <div className="card">
        <div className="card-hd">
          <div className="card-title">Últimas auditorías de mi empresa</div>
          <Link className="btn btn-sm" to="/cliente/reportes"><i className="ti ti-eye" /> Ver todas</Link>
        </div>
        <div className="tbl-wrap">
          <table className="tbl">
            <thead><tr><th>Área</th><th>Subcategoría</th><th>Cuestionario</th><th>Fecha</th><th>Puntaje</th><th>Estado</th></tr></thead>
            <tbody>
              <tr><td>Mantenimiento</td><td>Órdenes de trabajo</td><td>Generación y control</td><td>07 jun 2026</td><td><span className="badge b-ok">3.8 / 5</span></td><td><span className="badge b-ok">Completada</span></td></tr>
              <tr><td>Finanzas</td><td>Contabilidad</td><td>Costos de mantenimiento</td><td>02 jun 2026</td><td><span className="badge b-danger">1.9 / 5</span></td><td><span className="badge b-ok">Completada</span></td></tr>
              <tr><td>Administración</td><td>Recursos humanos</td><td>Evaluación de desempeño</td><td>28 may 2026</td><td><span className="badge b-ok">4.2 / 5</span></td><td><span className="badge b-ok">Completada</span></td></tr>
              <tr><td>Comercial</td><td>Fuerza de ventas</td><td>Gestión comercial</td><td>—</td><td>—</td><td><span className="badge b-info">Pendiente</span></td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div className="report-grid">
        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-hd"><div className="card-title">Puntaje por área</div></div>
          <div className="bar-chart">
            <div className="bar-col"><div className="bar-v">3.8</div><div className="bar-fill" style={{ height: 76, background: 'var(--brand-mid)' }} /><div className="bar-nm">Mant.</div></div>
            <div className="bar-col"><div className="bar-v">1.9</div><div className="bar-fill" style={{ height: 38, background: '#C0392B' }} /><div className="bar-nm">Finanzas</div></div>
            <div className="bar-col"><div className="bar-v">4.2</div><div className="bar-fill" style={{ height: 84, background: '#27AE60' }} /><div className="bar-nm">Admin.</div></div>
          </div>
        </div>
        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-hd"><div className="card-title">Plan de acción</div><span className="badge b-warn">3 pendientes</span></div>
          <div style={{ fontSize: 13, color: 'var(--text-2)', lineHeight: 1.8 }}>
            <div style={{ padding: '8px 0', borderBottom: '1px solid var(--border-light)', display: 'flex', gap: 8, alignItems: 'center' }}>
              <i className="ti ti-circle" style={{ color: '#C0392B', fontSize: 12 }} /><span>Implementar sistema formal de OT — <strong>Crítico</strong></span>
            </div>
            <div style={{ padding: '8px 0', borderBottom: '1px solid var(--border-light)', display: 'flex', gap: 8, alignItems: 'center' }}>
              <i className="ti ti-circle" style={{ color: '#D4860A', fontSize: 12 }} /><span>Definir KPIs de mantenimiento — <strong>Moderado</strong></span>
            </div>
            <div style={{ padding: '8px 0', display: 'flex', gap: 8, alignItems: 'center' }}>
              <i className="ti ti-circle" style={{ color: '#27AE60', fontSize: 12 }} /><span>Actualizar registros de costos — <strong>Leve</strong></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
