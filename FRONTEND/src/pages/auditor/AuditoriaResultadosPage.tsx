import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api, ApiError } from '../../api/client';
import { resultadosApi } from '../../api/resultadosApi';
import { useAppStore } from '../../store/useAppStore';
import { KpiCard } from '../../components/resultados/KpiCard';
import { CategoriasProgreso } from '../../components/resultados/CategoriasProgreso';
import type {
  Auditoria, AuditoriaCuestionario, Cuestionario, ResumenAuditoria, Subcategoria,
} from '../../types/domain';

const META_DEFECTO = 4.0;

interface CuestionarioAplicadoConNombre extends AuditoriaCuestionario {
  nombre: string;
  categoria: string;
}

/** "la categoría X" / "las categorías X y Y" / "las categorías X, Y y Z" — para nombrar lo que falta en vez de hablar en abstracto de "el catálogo". */
function formatearListaCategorias(nombres: string[]): string {
  if (nombres.length === 0) return 'las categorías pendientes';
  if (nombres.length === 1) return `la categoría ${nombres[0]}`;
  return `las categorías ${nombres.slice(0, -1).join(', ')} y ${nombres[nombres.length - 1]}`;
}

export function AuditoriaResultadosPage() {
  const { id: auditoriaId } = useParams<{ id: string }>();
  const { accessToken, mostrarToast } = useAppStore();

  const [auditoria, setAuditoria] = useState<Auditoria | null>(null);
  const [resumen, setResumen] = useState<ResumenAuditoria | null>(null);
  const [aplicados, setAplicados] = useState<CuestionarioAplicadoConNombre[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [generandoPdf, setGenerandoPdf] = useState(false);

  useEffect(() => {
    if (!auditoriaId) return;
    setCargando(true);
    setError(null);

    Promise.all([
      api.get<Auditoria>(`/api/auditorias/${auditoriaId}`, accessToken),
      resultadosApi.obtenerResumen(auditoriaId, accessToken, META_DEFECTO),
      api.get<AuditoriaCuestionario[]>(`/api/auditorias/${auditoriaId}/cuestionarios`, accessToken),
    ])
      .then(async ([auditoriaRes, resumenRes, aplicadosRes]) => {
        setAuditoria(auditoriaRes);
        setResumen(resumenRes);

        // Cada cuestionario aplicado se etiqueta con el nombre de SU categoría (vía su subcategoría)
        // para poder mostrar "Detalle por sección" y la dona de subcategorías por separado por categoría.
        const conNombre = await Promise.all(
          aplicadosRes.map(async (ac) => {
            const cuestionario = await api.get<Cuestionario>(`/api/cuestionarios/${ac.cuestionarioId}`, accessToken);
            const subcategoria = await api.get<Subcategoria>(`/api/subcategorias/${cuestionario.subcategoriaId}`, accessToken);
            const categoria = resumenRes.subcategorias.find((s) => s.subcategoria === subcategoria.nombre)?.categoria ?? '';
            return { ...ac, nombre: cuestionario.nombre, categoria };
          }),
        );
        setAplicados(conNombre);
      })
      .catch((err) => setError(err instanceof ApiError ? err.message : 'No se pudieron cargar los resultados de la auditoría.'))
      .finally(() => setCargando(false));
  }, [auditoriaId, accessToken]);

  if (cargando) {
    return <div className="card empty"><div className="empty-t">Cargando resultados…</div></div>;
  }

  if (error || !auditoria || !resumen) {
    return (
      <div className="card empty">
        <i className="ti ti-alert-triangle" />
        <div className="empty-t">No se pudieron cargar los resultados</div>
        <div className="empty-s">{error ?? 'Intenta recargar la página.'}</div>
      </div>
    );
  }

  const auditoriaFinalizada = auditoria.estado === 'finalizada';
  const areaDebil = resumen.subcategorias
    .filter((s) => s.puntaje != null)
    .sort((a, b) => (a.puntaje ?? 0) - (b.puntaje ?? 0))[0];

  const categoriasPendientes = resumen.categorias.filter((c) => !c.completa).map((c) => c.categoria);
  const textoCategoriasPendientes = formatearListaCategorias(categoriasPendientes);

  const generarPdf = async () => {
    if (!auditoriaId) return;
    setGenerandoPdf(true);
    try {
      const reporte = await resultadosApi.generarReporte(auditoriaId, accessToken);
      if (reporte.rutaPdf) {
        window.open(reporte.rutaPdf, '_blank', 'noopener');
      }
      mostrarToast(auditoriaFinalizada ? 'Informe final generado' : 'Informe preliminar generado', 'ok');
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo generar el informe PDF.', 'warn');
    } finally {
      setGenerandoPdf(false);
    }
  };

  return (
    <div>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr auto 1fr', alignItems: 'center', marginBottom: 12, gap: 12 }}>
        <div />
        <div style={{ textAlign: 'center' }}>
          <div className="card-title" style={{ fontSize: 20 }}>Resultados de la auditoría</div>
          <div className="card-sub" style={{ fontSize: 14 }}>
            {auditoria.fechaFin ? `Finalizada el ${auditoria.fechaFin}` : `Iniciada el ${auditoria.fechaInicio}`}
          </div>
        </div>
        <button
          className="btn btn-primary"
          style={{ justifySelf: 'end' }}
          disabled={resumen.puntajeGlobal == null || generandoPdf}
          title={resumen.puntajeGlobal == null ? 'Responde al menos un cuestionario para generar el informe' : undefined}
          onClick={generarPdf}
        >
          <i className="ti ti-file-download" />{' '}
          {generandoPdf ? 'Generando…' : auditoriaFinalizada ? 'Generar informe final' : 'Generar informe preliminar'}
        </button>
      </div>
      {!auditoriaFinalizada && resumen.puntajeGlobal != null && (
        <p className="hint" style={{ marginBottom: 16 }}>
          <i className="ti ti-info-circle" /> La auditoría sigue en progreso: el PDF se genera como <strong>informe preliminar</strong>, con fecha de corte de hoy y el avance actual — no cuenta lo pendiente como cero.
        </p>
      )}
      {resumen.puntajeGlobal == null && (
        <p className="hint" style={{ marginBottom: 16 }}>
          <i className="ti ti-info-circle" /> Responde al menos un cuestionario para poder generar el informe.
        </p>
      )}

      {/* Orden de presentación pedido por el usuario: por cada categoría, todo junto en su propia
          tarjeta — radar+histograma, grid de subcategorías, detalle por sección y dona de
          calificaciones (ver CategoriasProgreso) — y solo después el resumen ejecutivo
          (calificación general/avance, KPI, área más débil). */}
      <div style={{ marginBottom: 20 }}>
        <CategoriasProgreso
          auditoriaId={auditoriaId!}
          empresaId={auditoria.empresaId}
          categorias={resumen.categorias}
          subcategorias={resumen.subcategorias}
          aplicados={aplicados}
        />
      </div>

      {/* Calificación general: solo es "oficial" cuando TODO el catálogo activo quedó evaluado — mientras tanto se muestra el avance. */}
      <div
        className="card"
        style={{
          marginBottom: 20,
          borderColor: resumen.catalogoCompleto ? resumen.colorSemaforo : undefined,
          background: resumen.catalogoCompleto ? `${resumen.colorSemaforo}0D` : undefined,
        }}
      >
        {resumen.catalogoCompleto ? (
          <>
            <div className="card-title" style={{ color: resumen.colorSemaforo }}>
              <i className="ti ti-trophy" /> Auditoría integral completa
            </div>
            <div className="card-sub" style={{ marginTop: 4 }}>
              Las {resumen.subcategoriasTotal} subcategorías del catálogo quedaron evaluadas.
            </div>
            <div style={{ fontSize: 32, fontWeight: 700, color: resumen.colorSemaforo, marginTop: 12 }}>
              {resumen.puntajeGlobal?.toFixed(1)} / 5.0
            </div>
            <div style={{ fontSize: 13, color: 'var(--text-2)' }}>Calificación general — {resumen.nivelMadurez}</div>
          </>
        ) : (
          <>
            <div className="card-hd" style={{ marginBottom: 8 }}>
              <div>
                <div className="card-title">Avance de la auditoría integral</div>
                <div className="card-sub">
                  {resumen.subcategoriasEvaluadas} de {resumen.subcategoriasTotal} subcategorías del catálogo evaluadas
                  {' · '}{resumen.subcategoriasEnAlcance} en alcance (aplicadas) · {resumen.subcategoriasTotal - resumen.subcategoriasEnAlcance} sin iniciar
                </div>
              </div>
            </div>
            <div style={{ height: 8, borderRadius: 4, background: 'var(--surface-2)', overflow: 'hidden' }}>
              <div
                style={{
                  height: '100%',
                  width: `${resumen.subcategoriasTotal > 0 ? Math.round((resumen.subcategoriasEvaluadas / resumen.subcategoriasTotal) * 100) : 0}%`,
                  background: 'var(--brand-mid)',
                  borderRadius: 4,
                }}
              />
            </div>
            <div className="hint" style={{ marginTop: 8 }}>
              La calificación general y el gráfico de radar se generan una vez se complete el 100% de la evaluación en {textoCategoriasPendientes}.
            </div>
          </>
        )}
      </div>

      {/* Nivel 1 — Resumen ejecutivo */}
      <div className="g2" style={{ marginBottom: 20, alignItems: 'stretch' }}>
        <KpiCard
          puntajeGlobal={resumen.puntajeGlobal}
          nivelMadurez={resumen.nivelMadurez}
          colorSemaforo={resumen.colorSemaforo}
          preguntasRespondidas={resumen.preguntasRespondidas}
          preguntasEsperadas={resumen.preguntasEsperadas}
        />
        <div className="card" style={{ marginBottom: 0 }}>
          <div className="card-hd"><div className="card-title">Área más débil</div></div>
          {areaDebil ? (
            <>
              <div style={{ fontSize: 24, fontWeight: 700, color: areaDebil.colorSemaforo }}>{areaDebil.puntaje?.toFixed(1)} / 5.0</div>
              <div style={{ fontSize: 13, color: 'var(--text-2)', marginTop: 4 }}>{areaDebil.subcategoria}</div>
              <div className="hint" style={{ marginTop: 8 }}>Meta: {areaDebil.meta.toFixed(1)}</div>
            </>
          ) : (
            <p className="hint">Sin cuestionarios aplicados aún.</p>
          )}
        </div>
      </div>

      <p style={{ marginTop: 20 }}>
        <Link className="btn btn-sm" to="/auditor/reportes"><i className="ti ti-arrow-left" /> Volver a reportes</Link>
      </p>
    </div>
  );
}
