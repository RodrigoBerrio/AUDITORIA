import { useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { categorias as categoriasIniciales, subcategorias as subcategoriasIniciales } from '../../data/mockData';
import type { Categoria, Subcategoria } from '../../types/domain';
import { CategoryTree } from '../../components/ui/CategoryTree';
import { useAppStore } from '../../store/useAppStore';

type NodoSeleccionado = { tipo: 'categoria' | 'subcategoria'; id: string; categoriaId?: string } | null;

/**
 * Gestión de categorías/subcategorías.
 * Sustituye renderTree(), addNewSubcatRow(), saveNewCategory() (creación
 * manual de nodos DOM + arrays sincronizados a mano) por estado de React:
 * las listas de categorías/subcategorías viven en useState y el árbol
 * se re-renderiza solo cuando cambian.
 */
export function CategoriasPage() {
  const navigate = useNavigate();
  const { mostrarToast } = useAppStore();

  const [categorias, setCategorias] = useState<Categoria[]>(categoriasIniciales);
  const [subcategorias, setSubcategorias] = useState<Subcategoria[]>(subcategoriasIniciales);
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

  const subcategoriasPorCategoria = useMemo(() => {
    const map: Record<string, Subcategoria[]> = {};
    for (const s of subcategorias) {
      (map[s.categoriaId] ??= []).push(s);
    }
    return map;
  }, [subcategorias]);

  const categoriaSeleccionadaId =
    seleccionado?.tipo === 'categoria' ? seleccionado.id : seleccionado?.categoriaId;
  const categoriaActual = categorias.find((c) => c.id === categoriaSeleccionadaId) ?? null;
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

  const agregarCategoriaDesdeArbol = (nombre: string) => {
    const id = 'cat_' + nombre.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_]/g, '') + '_' + Date.now();
    setCategorias((prev) => [...prev, { id, nombre, esPlantilla: true, activo: true }]);
    mostrarToast('Categoría creada correctamente', 'ok');
  };

  const agregarFilaSubcat = () => setNuevasSubcats((prev) => [...prev, '']);
  const cambiarFilaSubcat = (idx: number, valor: string) =>
    setNuevasSubcats((prev) => prev.map((v, i) => (i === idx ? valor : v)));
  const quitarFilaSubcat = (idx: number) => setNuevasSubcats((prev) => prev.filter((_, i) => i !== idx));

  const crearNuevaCategoriaCompleta = () => {
    if (!nuevaCatNombre.trim()) return;
    const nombresValidos = nuevasSubcats.map((s) => s.trim()).filter(Boolean);
    if (nombresValidos.length === 0) {
      mostrarToast('Agrega al menos una subcategoría', 'warn');
      return;
    }
    const catId = 'cat_' + nuevaCatNombre.toLowerCase().replace(/\s+/g, '_').replace(/[^a-z0-9_]/g, '') + '_' + Date.now();
    const nuevaCategoria: Categoria = { id: catId, nombre: nuevaCatNombre.trim(), esPlantilla: true, activo: true };
    const nuevasSub: Subcategoria[] = nombresValidos.map((nombre, i) => ({
      id: `${catId}_sub_${i}`, categoriaId: catId, nombre, esPlantilla: true, activo: true,
    }));
    setCategorias((prev) => [...prev, nuevaCategoria]);
    setSubcategorias((prev) => [...prev, ...nuevasSub]);
    setSeleccionado({ tipo: 'categoria', id: catId });
    setMostrarWizardCat(false);
    setNuevaCatNombre('');
    setNuevasSubcats(['']);
    mostrarToast('Categoría y subcategorías creadas', 'ok');
  };

  const guardarSubcategoria = (irAlFormulario: boolean) => {
    if (!categoriaActual) {
      mostrarToast('Selecciona primero una categoría', 'warn');
      return;
    }
    if (subcatSeleccionId === '__new__') {
      if (!nuevaSubcatNombre.trim()) {
        mostrarToast('Escribe el nombre de la subcategoría', 'warn');
        return;
      }
      const id = `${categoriaActual.id}_sub_${Date.now()}`;
      setSubcategorias((prev) => [...prev, {
        id, categoriaId: categoriaActual.id, nombre: nuevaSubcatNombre.trim(),
        descripcion: descripcionArea, esPlantilla, responsable, activo: true,
      }]);
      setNuevaSubcatNombre('');
    }
    mostrarToast('Subcategoría guardada correctamente', 'ok');
    if (irAlFormulario) navigate('/auditor/formulario');
  };

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
                      <button className="btn btn-primary" onClick={crearNuevaCategoriaCompleta}>
                        <i className="ti ti-check" /> Crear categoría y continuar
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
                <button className="btn btn-primary" onClick={() => guardarSubcategoria(true)}>
                  <i className="ti ti-arrow-right" /> Guardar e ir al formulario
                </button>
                <button className="btn" onClick={() => guardarSubcategoria(false)}>
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
