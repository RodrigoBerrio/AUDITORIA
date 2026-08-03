export function ClienteReportesPage() {
  return (
    <div className="card">
      <div className="card-hd"><div className="card-title">Mis reportes de auditoría</div><button className="btn btn-sm"><i className="ti ti-download" /> Descargar todos</button></div>
      <div className="tbl-wrap">
        <table className="tbl">
          <thead><tr><th>Área</th><th>Cuestionario</th><th>Fecha</th><th>Puntaje</th><th>Auditor</th><th /></tr></thead>
          <tbody>
            <tr><td>Mantenimiento — Órdenes de trabajo</td><td>Generación y control</td><td>07 jun 2026</td><td><span className="badge b-ok">3.8 / 5</span></td><td>Oscar A. Berrío R.</td><td><div className="t-actions"><button className="btn btn-sm"><i className="ti ti-eye" /> Ver</button><button className="btn btn-sm"><i className="ti ti-download" /></button></div></td></tr>
            <tr><td>Finanzas — Contabilidad</td><td>Costos de mantenimiento</td><td>02 jun 2026</td><td><span className="badge b-danger">1.9 / 5</span></td><td>Oscar A. Berrío R.</td><td><div className="t-actions"><button className="btn btn-sm"><i className="ti ti-eye" /> Ver</button><button className="btn btn-sm"><i className="ti ti-download" /></button></div></td></tr>
            <tr><td>Administración — RRHH</td><td>Evaluación de desempeño</td><td>28 may 2026</td><td><span className="badge b-ok">4.2 / 5</span></td><td>Oscar A. Berrío R.</td><td><div className="t-actions"><button className="btn btn-sm"><i className="ti ti-eye" /> Ver</button><button className="btn btn-sm"><i className="ti ti-download" /></button></div></td></tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
