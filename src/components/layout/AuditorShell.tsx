import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
import { usuarioActual } from '../../data/mockData';
import { ConfirmModal } from '../ui/ConfirmModal';
import { ToastHost } from '../ui/ToastHost';

const NAV_PRINCIPAL = [
  { to: '/auditor/dashboard', icon: 'ti-layout-dashboard', label: 'Dashboard' },
  { to: '/auditor/empresas', icon: 'ti-building', label: 'Empresas' },
];
const NAV_AUDITORIA = [
  { to: '/auditor/categorias', icon: 'ti-folder', label: 'Categorías' },
  { to: '/auditor/formulario', icon: 'ti-checklist', label: 'Formulario' },
  { to: '/auditor/reportes', icon: 'ti-chart-bar', label: 'Reportes' },
];

/** Layout persistente del rol auditor/admin: sidebar + topbar + contenido de la ruta activa. */
export function AuditorShell() {
  const navigate = useNavigate();
  const { logout, pedirConfirmacion } = useAppStore();

  const cerrarSesion = () => {
    pedirConfirmacion('¿Cerrar sesión?', 'Volverás a la página principal.', 'Cerrar sesión', () => {
      logout();
      navigate('/');
    });
  };

  const iniciales = usuarioActual.nombre.split(' ').map((p) => p[0]).slice(0, 2).join('');

  return (
    <div className="shell">
      <aside className="sidebar" role="navigation" aria-label="Menú principal">
        <div className="sidebar-logo">
          <div className="logo-icon"><i className="ti ti-clipboard-check" /></div>
          <div>
            <div className="logo-name">Auditorías Industriales SAS</div>
            <div className="logo-tag">Panel del auditor</div>
          </div>
        </div>

        <div className="sidebar-section">
          <div className="sidebar-label">Principal</div>
          {NAV_PRINCIPAL.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
              <i className={`ti ${item.icon}`} /> {item.label}
            </NavLink>
          ))}
        </div>

        <div className="sidebar-section">
          <div className="sidebar-label">Auditoría</div>
          {NAV_AUDITORIA.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
              <i className={`ti ${item.icon}`} /> {item.label}
            </NavLink>
          ))}
        </div>

        <div className="sidebar-footer">
          <div className="avatar">{iniciales}</div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="user-name">{usuarioActual.nombre}</div>
            <div className="user-role">Auditor certificado</div>
          </div>
          <button className="icon-btn-white" title="Cerrar sesión" aria-label="Cerrar sesión" onClick={cerrarSesion}>
            <i className="ti ti-logout" />
          </button>
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <div className="topbar-left">
            <button className="btn btn-sm" onClick={() => navigate('/')}>
              <i className="ti ti-arrow-left" /> Volver al inicio
            </button>
          </div>
          <div className="topbar-right">
            <div className="rel">
              <button className="btn btn-ghost btn-sm" aria-label="Notificaciones">
                <i className="ti ti-bell" style={{ fontSize: 18 }} />
              </button>
              <div className="notif" />
            </div>
            <button className="btn btn-sm" onClick={() => navigate('/auditor/empresas')}>
              <i className="ti ti-plus" /> Nueva empresa
            </button>
            <button className="btn btn-primary btn-sm" onClick={() => navigate('/auditor/formulario')}>
              <i className="ti ti-player-play" /> Iniciar auditoría
            </button>
          </div>
        </header>

        <div className="content" role="main">
          <Outlet />
        </div>
      </div>

      <ConfirmModal />
      <ToastHost />
    </div>
  );
}
