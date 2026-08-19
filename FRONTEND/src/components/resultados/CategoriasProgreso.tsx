import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { RadarMadurez } from './RadarMadurez';
import { RankingBarras } from './RankingBarras';
import { NIVELES_DETALLE } from '../../config/semaforoDetalle';
import { resultadosApi } from '../../api/resultadosApi';
import { useAppStore } from '../../store/useAppStore';
import { ApiError } from '../../api/client';
import type { ItemPuntaje, PuntajeCategoria, PuntajeSubcategoria } from '../../types/domain';

interface CategoriasProgresoProps {
  auditoriaId: string;
  empresaId: string;
  categorias: PuntajeCategoria[];
  subcategorias: PuntajeSubcategoria[];
}

/** Anillo de progreso (SVG) — reemplaza la pill de texto "En progreso" por un indicador visual del avance de la categoría. */
function AnilloProgreso({ completadas, total, size = 56 }: { completadas: number; total: number; size?: number }) {
  const stroke = 6;
  const radio = (size - stroke) / 2;
  const circunferencia = 2 * Math.PI * radio;
  const fraccion = total > 0 ? completadas / total : 0;
  const offset = circunferencia * (1 - fraccion);
  return (
    <div style={{ position: 'relative', width: size, height: size, flexShrink: 0 }}>
      <svg width={size} height={size} style={{ transform: 'rotate(-90deg)' }}>
        <circle cx={size / 2} cy={size / 2} r={radio} fill="none" stroke="var(--surface-2)" strokeWidth={stroke} />
        <circle
          cx={size / 2} cy={size / 2} r={radio} fill="none" stroke="var(--brand-mid)" strokeWidth={stroke}
          strokeDasharray={circunferencia} strokeDashoffset={offset} strokeLinecap="round"
          style={{ transition: 'stroke-dashoffset 0.4s ease' }}
        />
      </svg>
      <div
        style={{
          position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center',
          fontSize: 14, fontWeight: 700, color: 'var(--text-1)',
        }}
      >
        {completadas}/{total}
      </div>
    </div>
  );
}

/** Vuelve a la auditoría activa (empresa + auditoría en el store) y manda al formulario para seguir respondiendo cuestionarios. */
function BotonReanudarAuditoria({ auditoriaId, empresaId }: { auditoriaId: string; empresaId: string }) {
  const navigate = useNavigate();
  const { setAuditoriaActiva, setEmpresaActiva } = useAppStore();

  const reanudar = () => {
    setEmpresaActiva(empresaId);
    setAuditoriaActiva(auditoriaId);
    navigate('/auditor/formulario');
  };

  return (
    <button className="btn btn-sm btn-primary" onClick={reanudar} title="Ir a los cuestionarios y seguir respondiendo esta auditoría">
      <i className="ti ti-player-play" /> Reanudar auditoría
    </button>
  );
}

/** Botón genérico para generar un informe independiente (Etapa 5) de una categoría o subcategoría — misma llamada, mismo endpoint que el informe integral, solo con alcance. */
function BotonGenerarInforme({ auditoriaId, alcance, compacto }: { auditoriaId: string; alcance: { tipo: 'categoria' | 'subcategoria'; nombre: string }; compacto?: boolean }) {
  const { accessToken, mostrarToast } = useAppStore();
  const [generando, setGenerando] = useState(false);

  const generar = async () => {
    setGenerando(true);
    try {
      const reporte = await resultadosApi.generarReporte(auditoriaId, accessToken, alcance);
      if (reporte.rutaPdf) {
        window.open(reporte.rutaPdf, '_blank', 'noopener');
      }
      mostrarToast(`Informe de ${alcance.nombre} generado`, 'ok');
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo generar el informe.', 'warn');
    } finally {
      setGenerando(false);
    }
  };

  return (
    <button className={compacto ? 'btn btn-sm' : 'btn btn-sm btn-primary'} onClick={generar} disabled={generando} title={`Generar informe independiente de ${alcance.nombre}`}>
      <i className="ti ti-file-download" /> {generando ? 'Generando…' : compacto ? 'Informe' : 'Generar informe'}
    </button>
  );
}

/**
 * Regla de generación progresiva del reporte: una categoría solo obtiene su propio radar,
 * calificación e histograma cuando TODAS sus subcategorías quedan evaluadas (todos los
 * cuestionarios respondidos al 100%). El radar y el histograma se arman SOLO con las
 * subcategorías de ESA categoría — nunca mezclados con los de otra categoría (error corregido
 * del spec). Cada subcategoría completa recibe además su propio panel con el desglose interno de
 * su(s) cuestionario(s) (por sección si el catálogo las tiene, o un bloque por cuestionario si no).
 */
export function CategoriasProgreso({ auditoriaId, empresaId, categorias, subcategorias }: CategoriasProgresoProps) {
  if (categorias.length === 0) {
    return null;
  }

  return (
    <div>
      <div className="card-title" style={{ fontSize: 15, marginBottom: 4 }}>Resultados por categoría</div>
      <div className="card-sub" style={{ marginBottom: 12 }}>
        Cada categoría genera su propio radar, calificación e histograma al completar el 100% de sus subcategorías
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {categorias.map((cat) => {
          if (!cat.completa) {
            // Alcance inferido (no declarado): 0 subcategorías tocadas es "no iniciada", distinto de "en progreso" sin completar aún.
            const iniciada = cat.subcategoriasEnAlcance > 0;
            return (
              <div key={cat.categoria} className="card" style={{ marginBottom: 0 }}>
                <div className="card-hd">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                    <AnilloProgreso completadas={cat.subcategoriasCompletas} total={cat.subcategoriasTotal} />
                    <div>
                      <div className="card-title">Categoría: {cat.categoria}</div>
                      <div className="card-sub">
                        {iniciada
                          ? `En progreso — ${cat.subcategoriasCompletas} de ${cat.subcategoriasTotal} subcategorías evaluadas (${cat.subcategoriasEnAlcance} en alcance)`
                          : `No iniciada — ${cat.subcategoriasTotal} subcategorías sin evaluar`}
                      </div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                    {iniciada && <BotonReanudarAuditoria auditoriaId={auditoriaId} empresaId={empresaId} />}
                    {iniciada && <BotonGenerarInforme auditoriaId={auditoriaId} alcance={{ tipo: 'categoria', nombre: cat.categoria }} compacto />}
                  </div>
                </div>
                <div className="hint" style={{ marginTop: 4 }}>
                  Faltan {cat.subcategoriasTotal - cat.subcategoriasCompletas} subcategoría(s) para generar su radar y desglose.
                </div>
              </div>
            );
          }

          const subsCategoria = subcategorias.filter((s) => s.categoria === cat.categoria);
          return <CategoriaCompletaCard key={cat.categoria} auditoriaId={auditoriaId} categoria={cat} subcategorias={subsCategoria} />;
        })}
      </div>
    </div>
  );
}

function CategoriaCompletaCard({ auditoriaId, categoria, subcategorias }: { auditoriaId: string; categoria: PuntajeCategoria; subcategorias: PuntajeSubcategoria[] }) {
  const [vistaTabla, setVistaTabla] = useState(false);
  const zonaCritica = subcategorias.filter((s) => s.detalle.some((d) => d.puntaje != null && d.puntaje < 2)).length;

  const histogramaItems: ItemPuntaje[] = [...subcategorias]
    .sort((a, b) => (a.puntaje ?? 0) - (b.puntaje ?? 0))
    .map((s) => ({ etiqueta: s.subcategoria, puntaje: s.puntaje, colorSemaforo: s.colorSemaforo, evaluada: s.evaluada }));

  return (
    <div className="card" style={{ marginBottom: 0 }}>
      <div className="card-hd">
        <div>
          <div className="card-title">Categoría: {categoria.categoria}</div>
          <div className="card-sub">{categoria.subcategoriasTotal} de {categoria.subcategoriasTotal} subcategorías evaluadas — escala de 1 a 5 por subcategoría</div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span className="badge" style={{ background: `${categoria.colorSemaforo}1A`, color: categoria.colorSemaforo }}>
            <i className="ti ti-check" style={{ fontSize: 12 }} /> Completa — {categoria.puntaje?.toFixed(1)} / 5.0
          </span>
          <BotonGenerarInforme auditoriaId={auditoriaId} alcance={{ tipo: 'categoria', nombre: categoria.categoria }} />
        </div>
      </div>

      {/* Radar + histograma propios de la categoría — armados solo con sus subcategorías, nunca mezclados con otra categoría. */}
      <div className="g2" style={{ alignItems: 'stretch', marginBottom: 16 }}>
        <RadarMadurez subcategorias={subcategorias} />
        <RankingBarras titulo={`Histograma — ${categoria.categoria}`} subtitulo="De menor a mayor puntaje" items={histogramaItems} />
      </div>

      <div className="metrics" style={{ gridTemplateColumns: 'repeat(3, 1fr)', marginBottom: 16 }}>
        <div className="metric">
          <div className="metric-value">{categoria.puntaje?.toFixed(1)}</div>
          <div className="metric-label">Puntaje global</div>
        </div>
        <div className="metric t-ok">
          <div className="metric-value">{subcategorias.length}</div>
          <div className="metric-label">Subcategorías evaluadas</div>
        </div>
        <div className={zonaCritica > 0 ? 'metric t-danger' : 'metric'}>
          <div className="metric-value">{zonaCritica}</div>
          <div className="metric-label">Subcategorías en zona crítica</div>
        </div>
      </div>

      <div
        style={{
          display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'center',
          padding: '10px 14px', background: 'var(--surface-2)', borderRadius: 8, marginBottom: 16,
        }}
      >
        <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-2)' }}>Puntaje:</span>
        {NIVELES_DETALLE.map((n) => (
          <span key={n.etiqueta} style={{ display: 'flex', alignItems: 'center', gap: 6, fontSize: 12, color: 'var(--text-2)' }}>
            <span style={{ width: 10, height: 10, borderRadius: '50%', background: n.color, display: 'inline-block', flexShrink: 0 }} />
            {n.etiqueta} ({n.rango})
          </span>
        ))}
        <button className="btn btn-sm" style={{ marginLeft: 'auto' }} onClick={() => setVistaTabla((v) => !v)}>
          <i className={`ti ${vistaTabla ? 'ti-chart-bar' : 'ti-table'}`} /> {vistaTabla ? 'Ver como gráfico' : 'Ver como tabla'}
        </button>
      </div>

      {vistaTabla ? (
        <div className="tbl-wrap">
          <table className="tbl">
            <thead><tr><th>Subcategoría</th><th>Criterio</th><th>Puntaje</th></tr></thead>
            <tbody>
              {subcategorias.flatMap((s) => s.detalle.map((d) => (
                <tr key={`${s.subcategoria}-${d.etiqueta}`}>
                  <td>{s.subcategoria}</td>
                  <td>{d.etiqueta}</td>
                  <td style={{ color: d.colorSemaforo, fontWeight: 600 }}>{d.puntaje != null ? d.puntaje.toFixed(1) : '—'}</td>
                </tr>
              )))}
            </tbody>
          </table>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: 16 }}>
          {subcategorias.map((s) => (
            <RankingBarras
              key={s.subcategoria}
              titulo={`${s.subcategoria} · ${s.puntaje != null ? s.puntaje.toFixed(1) : '—'}`}
              items={s.detalle}
              mensajeVacio="Sin desglose disponible."
              headerExtra={<BotonGenerarInforme auditoriaId={auditoriaId} alcance={{ tipo: 'subcategoria', nombre: s.subcategoria }} compacto />}
            />
          ))}
        </div>
      )}
    </div>
  );
}
