import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  empresas, categorias, subcategorias, cuestionarios, preguntasPorCuestionario,
} from '../../data/mockData';
import { useAppStore, useProgresoCuestionario } from '../../store/useAppStore';
import { QuestionCard } from '../../components/ui/QuestionCard';
import type { ValorEscala } from '../../types/domain';

/**
 * Formulario de auditoría.
 * Sustituye subcatMap/questMap/populateSubcat()/populateQuestTabs()/pickScale()
 * del prototipo: la jerarquía categoría → subcategoría → cuestionario → pregunta
 * ahora se deriva de los datos (mockData, futura API) y las respuestas viven
 * en el store, no en el DOM.
 */
export function AuditoriaFormPage() {
  const navigate = useNavigate();
  const { empresaActivaId, cuestionarioActivoId, setCuestionarioActivo, respuestas, responder, mostrarToast, pedirConfirmacion } = useAppStore();

  const empresaActiva = empresas.find((e) => e.id === empresaActivaId) ?? empresas[0];
  const [categoriaId, setCategoriaId] = useState(categorias[0]?.id ?? '');

  const subcatsDeCategoria = useMemo(
    () => subcategorias.filter((s) => s.categoriaId === categoriaId),
    [categoriaId],
  );
  const [subcategoriaId, setSubcategoriaId] = useState(subcatsDeCategoria[0]?.id ?? '');

  const cuestionariosDeSubcat = useMemo(
    () => cuestionarios.filter((c) => c.subcategoriaId === subcategoriaId),
    [subcategoriaId],
  );

  const cuestionarioActivo = cuestionarios.find((c) => c.id === cuestionarioActivoId) ?? cuestionariosDeSubcat[0];
  const preguntas = cuestionarioActivo ? preguntasPorCuestionario[cuestionarioActivo.id] ?? [] : [];
  const preguntaIds = preguntas.map((p) => p.id);
  const progreso = useProgresoCuestionario(preguntaIds);

  const cambiarCategoria = (id: string) => {
    setCategoriaId(id);
    const primeraSubcat = subcategorias.find((s) => s.categoriaId === id);
    setSubcategoriaId(primeraSubcat?.id ?? '');
    setCuestionarioActivo('');
  };

  const cambiarSubcategoria = (id: string) => {
    setSubcategoriaId(id);
    const primerCuestionario = cuestionarios.find((c) => c.subcategoriaId === id);
    setCuestionarioActivo(primerCuestionario?.id ?? '');
  };

  const finalizarAuditoria = () => {
    pedirConfirmacion(
      'Finalizar auditoría',
      '¿Confirmas que respondiste todas las preguntas y deseas generar el reporte?',
      'Confirmar y generar reporte',
      () => navigate('/auditor/reportes'),
    );
  };

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
          <div className="ctx-lbl">Empresa</div>
          <div className="ctx-val">{empresaActiva?.razonSocial}</div>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Categoría</div>
          <select className="ctx-select" value={categoriaId} onChange={(e) => cambiarCategoria(e.target.value)}>
            {categorias.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
          </select>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Subcategoría</div>
          <select className="ctx-select" value={subcategoriaId} onChange={(e) => cambiarSubcategoria(e.target.value)}>
            {subcatsDeCategoria.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}
          </select>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Cuestionario</div>
          <div className="ctx-val">{cuestionarioActivo?.nombre ?? '—'}</div>
        </div>
        <div className="ctx-div" />
        <div>
          <div className="ctx-lbl">Progreso</div>
          <div className="ctx-val">{progreso.respondidas} / {progreso.total} preguntas</div>
        </div>
        <div className="ctx-prog">
          <div className="progress-bar" role="progressbar" aria-valuemin={0} aria-valuemax={progreso.total} aria-valuenow={progreso.respondidas}>
            <div className="progress-fill" style={{ width: `${progreso.total ? (progreso.respondidas / progreso.total) * 100 : 0}%` }} />
          </div>
        </div>
        <button className="btn btn-sm" style={{ marginLeft: 'auto', background: 'rgba(255,255,255,.15)', borderColor: 'rgba(255,255,255,.3)', color: '#fff' }}
          onClick={() => mostrarToast('Avance guardado correctamente', 'ok')}>
          <i className="ti ti-device-floppy" /> Guardar avance
        </button>
      </div>

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

      {preguntas.length === 0 ? (
        <div className="card empty">
          <i className="ti ti-checklist" />
          <div className="empty-t">Este cuestionario aún no tiene preguntas cargadas</div>
          <div className="empty-s">Agrégalas desde el catálogo de preguntas cuando el backend esté conectado.</div>
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
              onResponder={(valor, obs) => responder(p.id, valor, obs)}
            />
          ))}
        </div>
      )}

      <div className="form-footer" style={{ paddingTop: 0, border: 'none' }}>
        <button className="btn btn-primary" onClick={finalizarAuditoria}>
          <i className="ti ti-arrow-right" /> Finalizar y generar reporte
        </button>
        <button className="btn" onClick={() => mostrarToast('Avance guardado correctamente', 'ok')}>
          <i className="ti ti-device-floppy" /> Guardar avance
        </button>
        <button className="btn btn-ghost"><i className="ti ti-notes" /> Agregar observación general</button>
      </div>
    </div>
  );
}
