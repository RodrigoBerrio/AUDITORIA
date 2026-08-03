import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
import { api, ApiError } from '../../api/client';
import type { Empresa } from '../../types/domain';

interface FormEmpresa {
  razonSocial: string; nit: string; sector: string; numEmpleados: string;
  ciudad: string; departamento: string; codigoPostal: string;
  contacto: string; telefono: string; correo: string; descripcion: string;
}
const EMPTY_FORM: FormEmpresa = {
  razonSocial: '', nit: '', sector: '', numEmpleados: '', ciudad: '', departamento: '',
  codigoPostal: '', contacto: '', telefono: '', correo: '', descripcion: '',
};

export function EmpresasPage() {
  const navigate = useNavigate();
  const { mostrarToast, pedirConfirmacion, setEmpresaActiva, accessToken } = useAppStore();
  const [form, setForm] = useState<FormEmpresa>(EMPTY_FORM);
  const [empresas, setEmpresas] = useState<Empresa[]>([]);
  const [cargando, setCargando] = useState(true);
  const [guardando, setGuardando] = useState(false);

  const cargarEmpresas = () => {
    setCargando(true);
    api.get<Empresa[]>('/api/empresas', accessToken)
      .then(setEmpresas)
      .catch(() => mostrarToast('No se pudo cargar la lista de empresas', 'warn'))
      .finally(() => setCargando(false));
  };

  useEffect(cargarEmpresas, [accessToken]);

  const set = <K extends keyof FormEmpresa>(k: K, v: FormEmpresa[K]) => setForm((f) => ({ ...f, [k]: v }));

  const requeridosOk = form.razonSocial.trim() && form.nit.trim() && form.sector && form.numEmpleados;

  const guardarYContinuar = async () => {
    if (!requeridosOk) {
      mostrarToast('Completa los campos obligatorios', 'warn');
      return;
    }
    setGuardando(true);
    try {
      await api.post('/api/empresas', {
        razonSocial: form.razonSocial,
        nit: form.nit,
        sector: form.sector,
        numEmpleados: Number(form.numEmpleados),
        ciudad: form.ciudad || null,
        departamento: form.departamento || null,
        codigoPostal: form.codigoPostal || null,
        contacto: form.contacto || null,
        telefono: form.telefono || null,
        correo: form.correo || null,
        descripcion: form.descripcion || null,
      }, accessToken);
      mostrarToast('Empresa guardada correctamente', 'ok');
      setForm(EMPTY_FORM);
      cargarEmpresas();
      navigate('/auditor/categorias');
    } catch (err) {
      mostrarToast(err instanceof ApiError ? err.message : 'No se pudo guardar la empresa', 'warn');
    } finally {
      setGuardando(false);
    }
  };

  const cancelar = () => {
    pedirConfirmacion('¿Descartar cambios?', 'Se perderá la información ingresada.', 'Descartar', () => setForm(EMPTY_FORM));
  };

  return (
    <div>
      <div className="card">
        <div className="card-hd">
          <div>
            <div className="card-title">Registrar empresa cliente</div>
            <div className="card-sub">Datos generales recopilados en la primera visita</div>
          </div>
          <span className="badge b-info"><i className="ti ti-circle-plus" style={{ fontSize: 12 }} /> Nueva</span>
        </div>

        <div className="g2">
          <div className="fg">
            <label className="lbl">Razón social <span className="req">*</span></label>
            <input className="inp" placeholder="Ej: Industrias Palma SAS" value={form.razonSocial} onChange={(e) => set('razonSocial', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">NIT <span className="req">*</span></label>
            <input className="inp" placeholder="900.123.456-7" value={form.nit} onChange={(e) => set('nit', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">Sector económico <span className="req">*</span></label>
            <select className="inp" value={form.sector} onChange={(e) => set('sector', e.target.value)}>
              <option value="">Seleccionar…</option>
              <option>Manufactura</option><option>Agroindustria</option>
              <option>Comercio</option><option>Servicios</option>
              <option>Construcción</option><option>Tecnología</option>
            </select>
          </div>
          <div className="fg">
            <label className="lbl">N° de empleados <span className="req">*</span></label>
            <input className="inp" type="number" placeholder="Entre 5 y 100" min={5} max={500} value={form.numEmpleados} onChange={(e) => set('numEmpleados', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">Ciudad</label>
            <input className="inp" placeholder="Ej: Cali" value={form.ciudad} onChange={(e) => set('ciudad', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">Departamento</label>
            <input className="inp" placeholder="Ej: Valle del Cauca" value={form.departamento} onChange={(e) => set('departamento', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">Código postal</label>
            <input className="inp" placeholder="Ej: 760001" maxLength={10} value={form.codigoPostal} onChange={(e) => set('codigoPostal', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">Contacto principal</label>
            <input className="inp" placeholder="Nombre del encargado o gerente" value={form.contacto} onChange={(e) => set('contacto', e.target.value)} />
          </div>
          <div className="fg">
            <label className="lbl">Teléfono de contacto</label>
            <input className="inp" type="tel" placeholder="+57 300 000 0000" value={form.telefono} onChange={(e) => set('telefono', e.target.value)} />
          </div>
          <div className="fg full">
            <label className="lbl">Correo corporativo</label>
            <input className="inp" type="email" placeholder="gerencia@empresa.com" value={form.correo} onChange={(e) => set('correo', e.target.value)} />
          </div>
          <div className="fg full">
            <label className="lbl">Descripción de la empresa</label>
            <textarea className="inp" placeholder="Actividad principal, productos, observaciones relevantes para la auditoría…" value={form.descripcion} onChange={(e) => set('descripcion', e.target.value)} />
          </div>
        </div>

        <div className="form-footer">
          <button className="btn btn-primary" onClick={guardarYContinuar} disabled={guardando}>
            <i className="ti ti-arrow-right" /> {guardando ? 'Guardando…' : 'Guardar y definir áreas a auditar'}
          </button>
          <button className="btn" onClick={() => mostrarToast('Borrador guardado', 'warn')}>
            <i className="ti ti-device-floppy" /> Guardar borrador
          </button>
          <button className="btn btn-ghost" style={{ marginLeft: 'auto', color: 'var(--text-3)' }} onClick={cancelar}>
            Cancelar
          </button>
        </div>
      </div>

      <div className="card">
        <div className="card-hd">
          <div>
            <div className="card-title">Empresas registradas</div>
            <div className="card-sub">{empresas.length} clientes activos — puede regresar en cualquier momento</div>
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            <input className="inp" style={{ width: 200, padding: '6px 10px', fontSize: 13 }} placeholder="Buscar empresa…" aria-label="Buscar empresa" />
            <button className="btn btn-sm"><i className="ti ti-filter" /> Filtrar</button>
          </div>
        </div>
        <div className="tbl-wrap">
          <table className="tbl">
            <thead>
              <tr><th>Empresa</th><th>Sector</th><th>Empleados</th><th>Ciudad</th><th>Auditorías</th><th>Última visita</th><th /></tr>
            </thead>
            <tbody>
              {cargando && (
                <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-3)' }}>Cargando empresas…</td></tr>
              )}
              {!cargando && empresas.length === 0 && (
                <tr><td colSpan={7} style={{ textAlign: 'center', color: 'var(--text-3)' }}>Todavía no hay empresas registradas.</td></tr>
              )}
              {empresas.map((e) => (
                <tr key={e.id}>
                  <td><strong>{e.razonSocial}</strong></td>
                  <td>{e.sector}</td><td>{e.numEmpleados}</td><td>{e.ciudad}</td>
                  <td><span className={`badge ${e.auditoriasRealizadas > 1 ? 'b-info' : 'b-gray'}`}>{e.auditoriasRealizadas}</span></td>
                  <td>{e.ultimaVisita}</td>
                  <td>
                    <div className="t-actions">
                      <button className="btn btn-sm"><i className="ti ti-eye" /> Ver</button>
                      <button className="btn btn-primary btn-sm" onClick={() => { setEmpresaActiva(e.id); navigate('/auditor/formulario'); }}>
                        <i className="ti ti-plus" /> Nueva auditoría
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
