import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ApiError } from '../../api/client';
import type { Categoria, Subcategoria } from '../../types/domain';
import { CategoryTree } from '../../components/ui/CategoryTree';
import { useAppStore } from '../../store/useAppStore';

type NodoSeleccionado = { tipo: 'categoria' | 'subcategoria'; id: string; categoriaId?: string } | null;

/**
 * Gestión de categorías/subcategorías — conectada a la API real
 * (catalogo/CategoriaController, SubcategoriaController). Las
 * subcategorías se cargan por categoría (GET /api/categorias/:id/subcategorias)
 * porque así está modelado el endpoint; se piden todas al cargar la página
 * para armar el árbol completo de una vez.
 */
export function CategoriasPage() {
  const navigate = useNavigate();
  const { mostrarToast, accessToken } = useAppStore();

  const [categorias, setCategorias] = useState<Categoria[]>([]);
  const [subcategoriasPorCategoria, setSubcategoriasPorCategoria] = useState<Record<string, Subcategoria[]>>({});
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);
  const [seleccionado, setSeleccionado] = useState<NodoSeleccionado>(null);

  // Wizard "nueva categoría"
  const [mostrarWizardCat, setMostrarWizardCat] = useState(false);
  const [nuevaCatNombre, setNuevaCatNombre] = useState('');
  const [nuevasSubcats, setNuevasSubcats] = useState<string[]>(['']);

  // Formulario subcategoría (paso 2 y 3)
  const [subcatSeleccionId, setSubcatSeleccionId] = useState<string>('__new__');
  const [nuevaSubcatNombre, setNuevaSubcatNombre] = useState('');
  const [descripcionArea, setDescripcionArea] = useState('');
  const [esPlantilla, setEsPlantilla] = useState(true);
  const [responsable, setResponsable] = useState('');

  const cargarCatalogo = async () => {
    setCargando(true);
    try {
      const cats = await api.get<Categoria[]>('/api/categorias', accessToken);
      setCategorias(cats);
      const entradas = await Promise.all(
        cats.map(async (c) => [c.id, await api.get<Subcategoria[]>(`/api/categorias/${c.id}/subcategorias`, accessToken)] as const),
      );
      setSubcategoriasPorCategoria(Object.fromEntries(entradas));
    } catch {
      mostrarToast('No se pudo cargar el catálogo de categorías', 'warn');
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => { cargarCatalogo(); }, [accessToken]);

  const categoriaSeleccionadaId =
    seleccionado?.tipo === 'categoria' ? seleccionado.id : seleccionado?.categoriaId;
  const categoriaActual = useMemo(
    () => categorias.find((c) => c.id === categoriaSeleccionadaId) ?? null,
    [categorias, categoriaSeleccionadaId],
  );
  const subcatsDeCategoriaActual = categoriaActual ? subcategoriasPorCategoria[categoriaActual.id] ?? [] : [];

  const handleSeleccionar = (nodo: NodoSeleccionado) => {
    setSeleccionado(nodo);
    setMostrarWizardCat(false);
    if (nodo?.tipo === 'subcategoria') {
      setSubcatSeleccionId(nodo.id);
    } else {
      setSubcatSeleccionId('__new__');
    }
  };

  const agregarCategoriaDesdeArbol = async (nombre: string) => {
    try {
      const creada = await api.post<Categoria>('/api/categorias', { nombre, esPlantilla: true }, accessToken);
      setCategorias((prev) => [...prev, creada]);
      setSubcategoriasPorCategoria((prev) => ({ ...prev, [creada.id]: [] }));
      mostrarToast('Categoría creada correctamente', 'ok');
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo crear la categoría', 'warn');
    }
  };

  const agregarFilaSubcat = () => setNuevasSubcats((prev) => [...prev, '']);
  const cambiarFilaSubcat = (idx: number, valor: string) =>
    setNuevasSubcats((prev) => prev.map((v, i) => (i === idx ? valor : v)));
  const quitarFilaSubcat = (idx: number) => setNuevasSubcats((prev) => prev.filter((_, i) => i !== idx));

  const crearNuevaCategoriaCompleta = async () => {
    if (!nuevaCatNombre.trim()) return;
    const nombresValidos = nuevasSubcats.map((s) => s.trim()).filter(Boolean);
    if (nombresValidos.length === 0) {
      mostrarToast('Agrega al menos una subcategoría', 'warn');
      return;
    }
    setGuardando(true);
    try {
      const nuevaCategoria = await api.post<Categoria>('/api/categorias', { nombre: nuevaCatNombre.trim(), esPlantilla: true }, accessToken);
      const nuevasSub = await Promise.all(
        nombresValidos.map((nombre) =>
          api.post<Subcategoria>(`/api/categorias/${nuevaCategoria.id}/subcategorias`, { nombre, esPlantilla: true }, accessToken)),
      );
      setCategorias((prev) => [...prev, nuevaCategoria]);
      setSubcategoriasPorCategoria((prev) => ({ ...prev, [nuevaCategoria.id]: nuevasSub }));
      setSeleccionado({ tipo: 'categoria', id: nuevaCategoria.id });
      setMostrarWizardCat(false);
      setNuevaCatNombre('');
      setNuevasSubcats(['']);
      mostrarToast('Categoría y subcategorías creadas', 'ok');
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo crear la categoría', 'warn');
    } finally {
      setGuardando(false);
    }
  };

  const guardarSubcategoria = async (irAlFormulario: boolean) => {
    if (!categoriaActual) {
      mostrarToast('Selecciona primero una categoría', 'warn');
      return;
    }
    if (subcatSeleccionId === '__new__') {
      if (!nuevaSubcatNombre.trim()) {
        mostrarToast('Escribe el nombre de la subcategoría', 'warn');
        return;
      }
      setGuardando(true);
      try {
        const creada = await api.post<Subcategoria>(`/api/categorias/${categoriaActual.id}/subcategorias`, {
          nombre: nuevaSubcatNombre.trim(),
          descripcion: descripcionArea || undefined,
          esPlantilla,
          responsable: responsable || undefined,
        }, accessToken);
        setSubcategoriasPorCategoria((prev) => ({
          ...prev,
          [categoriaActual.id]: [...(prev[categoriaActual.id] ?? []), creada],
        }));
        setNuevaSubcatNombre('');
      } catch (err) {
        mostrarToast(err instanceof ApiError ? err.message : 'No se pudo guardar la subcategoría', 'warn');
        setGuardando(false);
        return;
      }
      setGuardando(false);
    }
    mostrarToast('Subcategoría guardada correctamente', 'ok');
    if (irAlFormulario) navigate('/auditor/formulario');
  };

  if (cargando) {
    return <div className="card empty"><div className="empty-t">Cargando categorías…</div></div>;
  }

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '270px 1fr', gap: 20, alignItems: 'start' }}>
      <div className="card" style={{ marginBottom: 0 }}>
        <div className="card-hd" style={{ marginBottom: 12 }}>
          <div className="card-title">Áreas de la empresa</div>
          <button className="btn btn-primary btn-sm" title="Nueva categoría raíz" onClick={() => setMostrarWizardCat(true)}>
            <i className="ti ti-plus" />
          </button>
        </div>
        <p className="hint" style={{ marginBottom: 12 }}>
          <i className="ti ti-info-circle" /> Selecciona un nodo para editar o agrega áreas nuevas.
        </p>
        <CategoryTree
          categorias={categorias}
          subcategoriasPorCategoria={subcategoriasPorCategoria}
          seleccionado={seleccionado}
          onSeleccionar={handleSeleccionar}
          onAgregarCategoria={agregarCategoriaDesdeArbol}
        />
      </div>

      <div>
        <div className="card" style={{ marginBottom: 16 }}>
          <div className="card-hd" style={{ marginBottom: 4 }}>
            <div>
              <div className="card-title">Gestionar subcategoría</div>
              <div className="card-sub">Selecciona una categoría y subcategoría existente para editarla, o crea una nueva</div>
            </div>
          </div>

          <div style={{ background: 'var(--surface-2)', border: '1px solid var(--border-light)', borderRadius: 'var(--r-md)', padding: '14px 16px', marginBottom: 12 }}>
            <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--brand)', textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 10 }}>
              <i className="ti ti-circle-1" style={{ fontSize: 15, verticalAlign: -2 }} /> Categoría padre
            </div>
            <div className="fg full">
              <label className="lbl">Selecciona la categoría <span className="req">*</span></label>
              <select
                className="inp"
                value={mostrarWizardCat ? '__newcat__' : (categoriaActual?.id ?? '')}
                onChange={(e) => {
                  if (e.target.value === '__newcat__') { setMostrarWizardCat(true); return; }
                  setMostrarWizardCat(false);
                  setSeleccionado(e.target.value ? { tipo: 'categoria', id: e.target.value } : null);
                }}
              >
                <option value="">— Seleccionar categoría —</option>
                {categorias.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
                <option value="__newcat__" style={{ color: 'var(--brand)', fontWeight: 600 }}>✚ Crear nueva categoría…</option>
              </select>
            </div>

            {mostrarWizardCat && (
              <div style={{ marginTop: 14, paddingTop: 14, borderTop: '1px solid var(--border)' }}>
                <div style={{ fontSize: 12, fontWeight: 600, color: 'var(--brand)', marginBottom: 12 }}>
                  <i className="ti ti-wand" style={{ verticalAlign: -2 }} /> Nueva categoría
                </div>
                <div className="fg" style={{ marginBottom: 12 }}>
                  <label className="lbl">Nombre de la categoría <span className="req">*</span></label>
                  <input className="inp" placeholder="Ej: Logística, HSEQ, Gestión ambiental…" value={nuevaCatNombre} onChange={(e) => setNuevaCatNombre(e.target.value)} />
                </div>
                {nuevaCatNombre.trim().length > 1 && (
                  <div>
                    <label className="lbl" style={{ marginBottom: 6, display: 'block' }}>
                      Subcategorías <span className="req">*</span>
                      <span style={{ fontWeight: 400, color: 'var(--text-3)' }}> — agrega al menos una</span>
                    </label>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 8, marginBottom: 8 }}>
                      {nuevasSubcats.map((valor, idx) => (
                        <div key={idx} style={{ display: 'flex', gap: 8 }}>
                          <input className="inp" placeholder={`Subcategoría ${idx + 1}`} value={valor} onChange={(e) => cambiarFilaSubcat(idx, e.target.value)} />
                          {nuevasSubcats.length > 1 && (
                            <button className="btn btn-sm btn-ghost" onClick={() => quitarFilaSubcat(idx)}><i className="ti ti-x" /></button>
                          )}
                        </div>
                      ))}
                    </div>
                    <button className="btn btn-sm" type="button" onClick={agregarFilaSubcat}>
                      <i className="ti ti-plus" /> Agregar subcategoría
                    </button>
                    <div style={{ marginTop: 14, paddingTop: 12, borderTop: '1px solid var(--border-light)' }}>
                      <button className="btn btn-primary" onClick={crearNuevaCategoriaCompleta} disabled={guardando}>
                        <i className="ti ti-check" /> {guardando ? 'Creando…' : 'Crear categoría y continuar'}
                      </button>
                      <button className="btn" style={{ marginLeft: 8 }} onClick={() => { setMostrarWizardCat(false); setNuevaCatNombre(''); setNuevasSubcats(['']); }}>Cancelar</button>
                    </div>
                  </div>
                )}
              </div>
            )}
          </div>

          {categoriaActual && !mostrarWizardCat && (
            <>
              <div style={{ background: 'var(--surface-2)', border: '1px solid var(--border-light)', borderRadius: 'var(--r-md)', padding: '14px 16px', marginBottom: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--brand)', textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 10 }}>
                  <i className="ti ti-circle-2" style={{ fontSize: 15, verticalAlign: -2 }} /> Subcategoría
                </div>
                <div className="fg" style={{ marginBottom: 10 }}>
                  <label className="lbl">Subcategorías existentes en esta categoría</label>
                  <select className="inp" value={subcatSeleccionId} onChange={(e) => setSubcatSeleccionId(e.target.value)}>
                    <option value="__new__">✚ Crear nueva subcategoría…</option>
                    {subcatsDeCategoriaActual.map((s) => <option key={s.id} value={s.id}>{s.nombre}</option>)}
                  </select>
                </div>
                {subcatSeleccionId === '__new__' && (
                  <div className="fg">
                    <label className="lbl">Nombre de la nueva subcategoría <span className="req">*</span></label>
                    <input className="inp" placeholder="Ej: Mantenimiento hidráulico" value={nuevaSubcatNombre} onChange={(e) => setNuevaSubcatNombre(e.target.value)} />
                  </div>
                )}
              </div>

              <div style={{ background: 'var(--surface-2)', border: '1px solid var(--border-light)', borderRadius: 'var(--r-md)', padding: '14px 16px', marginBottom: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--brand)', textTransform: 'uppercase', letterSpacing: '.06em', marginBottom: 10 }}>
                  <i className="ti ti-circle-3" style={{ fontSize: 15, verticalAlign: -2 }} /> Detalles
                </div>
                <div className="g2">
                  <div className="fg full">
                    <label className="lbl">Descripción del área</label>
                    <textarea className="inp" style={{ minHeight: 58 }} placeholder="Alcance y responsabilidades del área a auditar…" value={descripcionArea} onChange={(e) => setDescripcionArea(e.target.value)} />
                  </div>
                  <div className="fg">
                    <label className="lbl">¿Guardar como plantilla reutilizable?</label>
                    <select className="inp" value={esPlantilla ? 'si' : 'no'} onChange={(e) => setEsPlantilla(e.target.value === 'si')}>
                      <option value="si">Sí — disponible para otras empresas</option>
                      <option value="no">No — solo para esta empresa</option>
                    </select>
                  </div>
                  <div className="fg">
                    <label className="lbl">Responsable del área</label>
                    <input className="inp" placeholder="Nombre del jefe de área" value={responsable} onChange={(e) => setResponsable(e.target.value)} />
                  </div>
                </div>
              </div>

              <div className="form-footer">
                <button className="btn btn-primary" onClick={() => guardarSubcategoria(true)} disabled={guardando}>
                  <i className="ti ti-arrow-right" /> Guardar e ir al formulario
                </button>
                <button className="btn" onClick={() => guardarSubcategoria(false)} disabled={guardando}>
                  <i className="ti ti-plus" /> Guardar y agregar otra
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
