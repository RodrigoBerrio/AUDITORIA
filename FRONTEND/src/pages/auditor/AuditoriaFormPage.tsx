import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ApiError } from '../../api/client';
import { resultadosApi } from '../../api/resultadosApi';
import { useAppStore, useProgresoCuestionario } from '../../store/useAppStore';
import { QuestionCard } from '../../components/ui/QuestionCard';
import type {
  Auditoria, AuditoriaCuestionario, Categoria, Cuestionario, Empresa, Pregunta, PuntajeSubcategoria, Respuesta, ValorEscala,
} from '../../types/domain';

type EstadoSubcategoria = 'completado' | 'en_progreso' | 'pendiente';

const ESTADO_SUBCATEGORIA_META: Record<EstadoSubcategoria, { icono: string; color: string; etiqueta: string }> = {
  completado: { icono: 'ti-check', color: 'var(--ok)', etiqueta: 'Completado' },
  en_progreso: { icono: 'ti-clock', color: 'var(--warn)', etiqueta: 'En progreso' },
  pendiente: { icono: 'ti-circle-dashed', color: 'var(--text-3)', etiqueta: 'Pendiente' },
};

/**
 * Selector de subcategoría con estado visible por fila (Completado / En progreso / Pendiente),
 * para que el auditor sepa de un vistazo qué le falta sin tener que ir a Resultados.
 */
function SelectorSubcategoria({
  opciones, valor, onChange,
}: { opciones: { id: string; nombre: string; estado: EstadoSubcategoria }[]; valor: string; onChange: (id: string) => void }) {
  const [abierto, setAbierto] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!abierto) return;
    const cerrar = (e: MouseEvent) => {
      if (ref.current && !ref.current.contains(e.target as Node)) setAbierto(false);
    };
    document.addEventListener('mousedown', cerrar);
    return () => document.removeEventListener('mousedown', cerrar);
  }, [abierto]);

  const actual = opciones.find((o) => o.id === valor);

  return (
    <div ref={ref} style={{ position: 'relative' }}>
      <button
        type="button"
        className="ctx-select"
        style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', gap: 6 }}
        onClick={() => setAbierto((v) => !v)}
      >
        <span>{actual?.nombre ?? '—'}</span>
        <i className="ti ti-chevron-down" style={{ fontSize: 12, transition: 'transform .15s', transform: abierto ? 'rotate(180deg)' : 'none' }} />
      </button>
      {abierto && (
        <div
          style={{
            position: 'absolute', top: 'calc(100% + 6px)', left: 0, minWidth: 300, zIndex: 50,
            background: '#fff', border: '1px solid var(--border)', borderRadius: 10,
            boxShadow: '0 8px 24px rgba(0,0,0,.18)', overflow: 'hidden',
          }}
        >
          {opciones.map((o) => {
            const meta = ESTADO_SUBCATEGORIA_META[o.estado];
            return (
              <button
                key={o.id}
                type="button"
                onClick={() => { onChange(o.id); setAbierto(false); }}
                style={{
                  display: 'flex', alignItems: 'center', gap: 10, width: '100%', padding: '9px 14px',
                  background: o.id === valor ? 'var(--brand-light)' : 'transparent', border: 'none',
                  borderBottom: '1px solid var(--surface-2)', cursor: 'pointer', textAlign: 'left',
                }}
              >
                <i className={`ti ${meta.icono}`} style={{ fontSize: 14, color: meta.color, flexShrink: 0, width: 14 }} />
                <span style={{ flex: 1, fontSize: 13, color: 'var(--text-1)', fontWeight: o.id === valor ? 600 : 500 }}>{o.nombre}</span>
                <span style={{ fontSize: 11.5, fontWeight: 600, color: meta.color, flexShrink: 0 }}>{meta.etiqueta}</span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

/**
 * Formulario de auditoría — conectado a la API real. La jerarquía
 * categoría → subcategoría → cuestionario → pregunta se carga del
 * catálogo real (catalogo/*); al elegir un cuestionario se aplica a la
 * auditoría activa (POST /api/auditorias/:id/cuestionarios, upsert local)
 * y cada respuesta se guarda de inmediato (PUT /api/auditoria-cuestionarios/:id/respuestas)
 * — no hay botón "guardar" que sincronice un estado local aparte.
 */
export function AuditoriaFormPage() {
  const navigate = useNavigate();
  const {
    empresaActivaId, auditoriaActivaId, cuestionarioActivoId, setCuestionarioActivo,
    setEmpresaActiva, setAuditoriaActiva,
    respuestas, responder, mostrarToast, pedirConfirmacion, accessToken,
  } = useAppStore();

  const [empresaDetalle, setEmpresaDetalle] = useState<Empresa | null>(null);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [auditoriaEnProgresoPorEmpresa, setAuditoriaEnProgresoPorEmpresa] = useState<Record<string, string>>({});
  const [cambiandoEmpresa, setCambiandoEmpresa] = useState(false);
  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [categoriaId, setCategoriaId] = useState('');
  const [subcategoriasDeCategoria, setSubcategoriasDeCategoria] = useState<{ id: string; nombre: string }[]>([]);
  const [subcategoriaId, setSubcategoriaId] = useState('');
  const [cuestionariosDeSubcat, setCuestionariosDeSubcat] = useState<Cuestionario[]>([]);
  const [preguntas, setPreguntas] = useState<Pregunta[]>([]);
  const [aplicados, setAplicados] = useState<Record<string, string>>({}); // cuestionarioId -> auditoriaCuestionarioId
  const [resumenSubs, setResumenSubs] = useState<PuntajeSubcategoria[]>([]);
  const [cargando, setCargando] = useState(true);
  const [cargandoPreguntas, setCargandoPreguntas] = useState(false);
  const [finalizando, setFinalizando] = useState(false);

  const cuestionarioActivo = cuestionariosDeSubcat.find((c) => c.id === cuestionarioActivoId);
  const preguntaIds = preguntas.map((p) => p.id);
  const progreso = useProgresoCuestionario(preguntaIds);

  // Estado (Completado / En progreso / Pendiente) de cada subcategoría de la categoría elegida —
  // mismo criterio evaluada/enAlcance que ResultadosService, para que el auditor lo vea sin salir del formulario.
  const categoriaActual = categorias.find((c) => c.id === categoriaId);
  const opcionesSubcategoria = subcategoriasDeCategoria.map((s) => {
    const info = resumenSubs.find((r) => r.categoria === categoriaActual?.nombre && r.subcategoria === s.nombre);
    const estado: EstadoSubcategoria = !info || !info.enAlcance ? 'pendiente' : info.evaluada ? 'completado' : 'en_progreso';
    return { id: s.id, nombre: s.nombre, estado };
  });

  const cargarResumen = () => {
    if (!auditoriaActivaId) return;
    resultadosApi.obtenerResumen(auditoriaActivaId, accessToken)
      .then((r) => setResumenSubs(r.subcategorias))
      .catch(() => {});
  };

  // Carga inicial: empresa activa, categorías, cuestionarios ya aplicados a esta auditoría (para reanudar)
  // y el resumen de avance (para el estado por subcategoría en el selector).
  useEffect(() => {
    if (!auditoriaActivaId || !empresaActivaId) return;
    setCargando(true);
    Promise.all([
      api.get<Empresa>(`/api/empresas/${empresaActivaId}`, accessToken),
      api.get<Categoria[]>('/api/categorias', accessToken),
      api.get<AuditoriaCuestionario[]>(`/api/auditorias/${auditoriaActivaId}/cuestionarios`, accessToken),
    ])
      .then(([empresa, cats, aplicadosRes]) => {
        setEmpresaDetalle(empresa);
        setCategorias(cats);
        setAplicados(Object.fromEntries(aplicadosRes.map((ac) => [ac.cuestionarioId, ac.id])));
        if (cats[0]) setCategoriaId(cats[0].id);
      })
      .catch(() => mostrarToast('No se pudo cargar el catálogo de auditoría', 'warn'))
      .finally(() => setCargando(false));
    cargarResumen();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [auditoriaActivaId, empresaActivaId, accessToken]);

  // Lista de empresas registradas (para el selector) y, por cada una, su auditoría en_progreso
  // si ya tiene una — para saber si "cambiar de empresa" debe reanudar o crear una auditoría nueva.
  useEffect(() => {
    Promise.all([
      api.get<Empresa[]>('/api/empresas', accessToken),
      api.get<Auditoria[]>('/api/auditorias', accessToken),
    ])
      .then(([emps, auds]) => {
        setEmpresas(emps);
        setAuditoriaEnProgresoPorEmpresa(
          Object.fromEntries(auds.filter((a) => a.estado === 'en_progreso').map((a) => [a.empresaId, a.id])),
        );
      })
      .catch(() => {});
  }, [accessToken]);

  const cambiarEmpresa = async (nuevaEmpresaId: string) => {
    if (!nuevaEmpresaId || nuevaEmpresaId === empresaActivaId) return;
    setCambiandoEmpresa(true);
    try {
      let auditoriaId = auditoriaEnProgresoPorEmpresa[nuevaEmpresaId];
      if (!auditoriaId) {
        const nueva = await api.post<Auditoria>('/api/auditorias', { empresaId: nuevaEmpresaId }, accessToken);
        auditoriaId = nueva.id;
        setAuditoriaEnProgresoPorEmpresa((prev) => ({ ...prev, [nuevaEmpresaId]: auditoriaId }));
      }
      setEmpresaActiva(nuevaEmpresaId);
      setAuditoriaActiva(auditoriaId);
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo cambiar de empresa', 'warn');
    } finally {
      setCambiandoEmpresa(false);
    }
  };

  // Subcategorías de la categoría elegida.
  useEffect(() => {
    if (!categoriaId) return;
    api.get<{ id: string; nombre: string }[]>(`/api/categorias/${categoriaId}/subcategorias`, accessToken)
      .then((subs) => {
        setSubcategoriasDeCategoria(subs);
        setSubcategoriaId(subs[0]?.id ?? '');
      })
      .catch(() => mostrarToast('No se pudo cargar las subcategorías', 'warn'));
  }, [categoriaId, accessToken]);

  // Cuestionarios de la subcategoría elegida.
  useEffect(() => {
    if (!subcategoriaId) { setCuestionariosDeSubcat([]); return; }
    api.get<Cuestionario[]>(`/api/subcategorias/${subcategoriaId}/cuestionarios`, accessToken)
      .then((cs) => {
        setCuestionariosDeSubcat(cs);
        setCuestionarioActivo(cs[0]?.id ?? '');
      })
      .catch(() => mostrarToast('No se pudo cargar los cuestionarios', 'warn'));
  }, [subcategoriaId, accessToken]);

  // Al activar un cuestionario: cargar sus preguntas, aplicarlo a la auditoría si aún no lo estaba,
  // y precargar las respuestas ya guardadas (para poder reanudar sin perder avance).
  useEffect(() => {
    if (!cuestionarioActivoId || !auditoriaActivaId) { setPreguntas([]); return; }
    let cancelado = false;
    setCargandoPreguntas(true);

    (async () => {
      try {
        const preguntasRes = await api.get<Pregunta[]>(`/api/cuestionarios/${cuestionarioActivoId}/preguntas`, accessToken);
        if (cancelado) return;
        setPreguntas(preguntasRes);

        let acId = aplicados[cuestionarioActivoId];
        if (!acId) {
          const ac = await api.post<AuditoriaCuestionario>(
            `/api/auditorias/${auditoriaActivaId}/cuestionarios`, { cuestionarioId: cuestionarioActivoId }, accessToken,
          );
          if (cancelado) return;
          acId = ac.id;
          setAplicados((prev) => ({ ...prev, [cuestionarioActivoId]: acId }));
        }

        const respuestasRes = await api.get<Respuesta[]>(`/api/auditoria-cuestionarios/${acId}/respuestas`, accessToken);
        if (cancelado) return;
        respuestasRes.forEach((r) => responder(r.preguntaId, r.valor, r.observacion));
      } catch (err) {
        if (!cancelado) mostrarToast(err instanceof ApiError ? err.message : 'No se pudo cargar el cuestionario', 'warn');
      } finally {
        if (!cancelado) setCargandoPreguntas(false);
      }
    })();

    return () => { cancelado = true; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cuestionarioActivoId, auditoriaActivaId]);

  const cambiarCategoria = (id: string) => {
    setCategoriaId(id);
    setCuestionarioActivo('');
  };

  const cambiarSubcategoria = (id: string) => {
    setSubcategoriaId(id);
    setCuestionarioActivo('');
  };

  /** Guarda localmente (para reflejo inmediato en UI) y persiste de una vez en el backend real. */
  const guardarRespuesta = (preguntaId: string, valor: ValorEscala, observacion?: string) => {
    responder(preguntaId, valor, observacion);
    const acId = cuestionarioActivoId ? aplicados[cuestionarioActivoId] : undefined;
    if (!acId || !valor) return;
    api.put(`/api/auditoria-cuestionarios/${acId}/respuestas`, { preguntaId, valor, observacion: observacion || null }, accessToken)
      .then(cargarResumen)
      .catch((err) => mostrarToast(err instanceof ApiError ? err.message : 'No se pudo guardar la respuesta', 'warn'));
  };

  const finalizarAuditoria = () => {
    pedirConfirmacion(
      'Finalizar auditoría',
      '¿Confirmas que respondiste todas las preguntas y deseas generar el reporte?',
      'Confirmar y generar reporte',
      async () => {
        if (!auditoriaActivaId) return;
        setFinalizando(true);
        try {
          await api.post(`/api/auditorias/${auditoriaActivaId}/finalizar`, {}, accessToken);
          mostrarToast('Auditoría finalizada correctamente', 'ok');
          navigate(`/auditor/auditorias/${auditoriaActivaId}/resultados`);
        } catch (err) {
          mostrarToast(err instanceof ApiError ? err.message : 'No se pudo finalizar la auditoría', 'warn');
        } finally {
          setFinalizando(false);
        }
      },
    );
  };

  if (!auditoriaActivaId || !empresaActivaId) {
    return (
      <div className="card empty">
        <i className="ti ti-building-plus" />
        <div className="empty-t">No hay una auditoría activa</div>
        <div className="empty-s">Ve a "Empresas" y presiona "Nueva auditoría" para empezar a responder un formulario.</div>
        <button className="btn btn-primary" style={{ marginTop: 12 }} onClick={() => navigate('/auditor/empresas')}>
          <i className="ti ti-arrow-right" /> Ir a empresas
        </button>
      </div>
    );
  }

  if (cargando) {
    return <div className="card empty"><div className="empty-t">Cargando auditoría…</div></div>;
  }

  if (categorias.length === 0) {
    return (
      <div className="card empty">
        <i className="ti ti-folder-off" />
        <div className="empty-t">Aún no hay categorías cargadas</div>
        <div className="empty-s">Crea una categoría, subcategoría y cuestionario para empezar a auditar.</div>
        <button className="btn btn-primary" style={{ marginTop: 12 }} onClick={() => navigate('/auditor/categorias')}>
          <i className="ti ti-plus" /> Ir a categorías
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="audit-ctx" role="status" aria-live="polite">
        <div>
          <div className="ctx-lbl">Auditoría a la empresa :</div>
          <select
            className="ctx-select"
            value={empresaActivaId ?? ''}
            onChange={(e) => cambiarEmpresa(e.target.value)}
            disabled={cambiandoEmpresa}
            title="Cambiar la empresa que se está auditando"
          >
            {empresaActivaId && !empresas.some((emp) => emp.id === empresaActivaId) && (
              <option value={empresaActivaId}>{empresaDetalle?.razonSocial ?? '—'}</option>
            )}
            {empresas.map((emp) => <option key={emp.id} value={emp.id}>{emp.razonSocial}</option>)}
          </select>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Categoría :</div>
          <select className="ctx-select" value={categoriaId} onChange={(e) => cambiarCategoria(e.target.value)}>
            {categorias.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </select>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Subcategoría :</div>
          <SelectorSubcategoria opciones={opcionesSubcategoria} valor={subcategoriaId} onChange={cambiarSubcategoria} />
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Cuestionario :</div>
          <div className="ctx-val">{cuestionarioActivo?.nombre ?? '—'}</div>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Progreso :</div>
          <div className="ctx-val">{progreso.respondidas} / {progreso.total} preguntas</div>
        </div>
        <div className="ctx-prog">
          <div className="progress-bar" role="progressbar" aria-valuemin={0} aria-valuemax={progreso.total} aria-valuenow={progreso.respondidas}>
            <div className="progress-fill" style={{ width: `${progreso.total ? (progreso.respondidas / progreso.total) * 100 : 0}%` }} />
          </div>
        </div>
      </div>

      {cuestionariosDeSubcat.length > 0 && (
        <div className="card" style={{ padding: '12px 16px', marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
            <span style={{ fontSize: 12, fontWeight: 600, color: 'var(--text-3)', marginRight: 4 }}>Cuestionario:</span>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {cuestionariosDeSubcat.map((c) => (
                <button
                  key={c.id}
                  className={`q-opt${cuestionarioActivo?.id === c.id ? ' sel' : ''}`}
                  onClick={() => setCuestionarioActivo(c.id)}
                >
                  {c.nombre}
                </button>
              ))}
            </div>
            <div style={{ marginLeft: 'auto' }} className="hint">
              <i className="ti ti-info-circle" /> Escala 1–5: 1 = básico/reactivo, 5 = avanzado/analítico
            </div>
          </div>
        </div>
      )}

      {cargandoPreguntas ? (
        <div className="card empty"><div className="empty-t">Cargando preguntas…</div></div>
      ) : preguntas.length === 0 ? (
        <div className="card empty">
          <i className="ti ti-checklist" />
          <div className="empty-t">
            {cuestionariosDeSubcat.length === 0 ? 'Esta subcategoría aún no tiene cuestionarios' : 'Este cuestionario aún no tiene preguntas cargadas'}
          </div>
          <div className="empty-s">Agrégalas desde el catálogo de preguntas.</div>
        </div>
      ) : (
        <div className="card">
          <div className="card-hd"><div className="card-title">{cuestionarioActivo?.nombre}</div></div>
          {preguntas.map((p, i) => (
            <QuestionCard
              key={p.id}
              pregunta={p}
              numero={i + 1}
              totalPreguntas={preguntas.length}
              valor={respuestas[p.id]?.valor as ValorEscala | undefined}
              observacion={respuestas[p.id]?.observacion}
              onResponder={(valor, obs) => guardarRespuesta(p.id, valor, obs)}
            />
          ))}
        </div>
      )}

      <div className="form-footer" style={{ paddingTop: 0, border: 'none' }}>
        <button className="btn btn-primary" onClick={finalizarAuditoria} disabled={finalizando}>
          <i className="ti ti-arrow-right" /> {finalizando ? 'Finalizando…' : 'Guardar respuestas'}
        </button>
        <button className="btn btn-ghost"><i className="ti ti-notes" /> Agregar observación general</button>
      </div>
    </div>
  );
}
