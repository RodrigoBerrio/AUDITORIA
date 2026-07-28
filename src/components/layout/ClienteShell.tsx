import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
import { ConfirmModal } from '../ui/ConfirmModal';
import { ToastHost } from '../ui/ToastHost';

const NAV_CLIENTE = [
  { to: '/cliente/resumen', icon: 'ti-layout-dashboard', label: 'Resumen' },
  { to: '/cliente/reportes', icon: 'ti-chart-bar', label: 'Mis reportes' },
  { to: '/cliente/hallazgos', icon: 'ti-alert-triangle', label: 'Hallazgos' },
];

/** Layout persistente del portal del cliente empresarial. */
export function ClienteShell() {
  const navigate = useNavigate();
  const { logout, pedirConfirmacion } = useAppStore();

  const cerrarSesion = () => {
    pedirConfirmacion('¿Cerrar sesión?', 'Volverás a la página principal.', 'Cerrar sesión', () => {
      logout();
      navigate('/');
    });
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div className="logo-icon"><i className="ti ti-clipboard-check" /></div>
          <div>
            <div className="logo-name">Auditorías Industriales SAS</div>
            <div className="logo-tag">Portal empresarial</div>
          </div>
        </div>
        <div className="sidebar-section">
          <div className="sidebar-label">Mi empresa</div>
          {NAV_CLIENTE.map((item) => (
            <NavLink key={item.to} to={item.to} className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
              <i className={`ti ${item.icon}`} /> {item.label}
            </NavLink>
          ))}
        </div>
        <div className="sidebar-footer">
          <div className="avatar" style={{ background: 'rgba(255,255,255,.2)' }}>IP</div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="user-name">Industrias Palma SAS</div>
            <div className="user-role">Cliente empresarial</div>
          </div>
          <button className="icon-btn-white" title="Cerrar sesión" onClick={cerrarSesion}>
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
            <span className="badge b-ok"><i className="ti ti-circle-check" style={{ fontSize: 12 }} /> Empresa verificada</span>
          </div>
        </header>
        <div className="content">
          <Outlet />
        </div>
      </div>

      <ConfirmModal />
      <ToastHost />
    </div>
  );
}
