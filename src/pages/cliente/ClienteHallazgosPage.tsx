import { hallazgos } from '../../data/mockData';

const SEVERIDAD_BADGE: Record<string, string> = { critica: 'b-danger', alta: 'b-danger', media: 'b-warn', baja: 'b-info' };
const ESTADO_BADGE: Record<string, string> = { abierto: 'b-warn', en_tratamiento: 'b-gray', cerrado: 'b-ok' };
const ESTADO_TEXTO: Record<string, string> = { abierto: 'Pendiente', en_tratamiento: 'En proceso', cerrado: 'Resuelto' };

export function ClienteHallazgosPage() {
  const activos = hallazgos.filter((h) => h.estado !== 'cerrado').length;
  return (
    <div className="card">
      <div className="card-hd"><div className="card-title">Hallazgos y plan de mejora</div><span className="badge b-warn">{activos} hallazgos activos</span></div>
      <div className="tbl-wrap">
        <table className="tbl">
          <thead><tr><th>Hallazgo</th><th>Área</th><th>Severidad</th><th>Acción recomendada</th><th>Estado</th></tr></thead>
          <tbody>
            {hallazgos.map((h) => (
              <tr key={h.id}>
                <td>{h.descripcion}</td>
                <td>{h.area}</td>
                <td><span className={`badge ${SEVERIDAD_BADGE[h.severidad]}`}>{h.severidad === 'critica' ? 'Crítico' : h.severidad === 'media' ? 'Moderado' : 'Leve'}</span></td>
                <td>{h.accionRecomendada}</td>
                <td><span className={`badge ${ESTADO_BADGE[h.estado]}`}>{ESTADO_TEXTO[h.estado]}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
