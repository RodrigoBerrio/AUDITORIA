import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAppStore } from '../../store/useAppStore';
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
  const { logout, pedirConfirmacion, usuario, auditoriaActivaId } = useAppStore();
  const [sidebarAbierto, setSidebarAbierto] = useState(false);

  const cerrarSesion = () => {
    pedirConfirmacion('¿Cerrar sesión?', 'Volverás a la página principal.', 'Cerrar sesión', () => {
      logout();
      navigate('/');
    });
  };

  const nombreUsuario = usuario?.nombre ?? 'Auditor';
  const iniciales = nombreUsuario.split(' ').map((p) => p[0]).slice(0, 2).join('');

  return (
    <div className="shell">
      {sidebarAbierto && <div className="sidebar-backdrop" onClick={() => setSidebarAbierto(false)} />}
      <aside className={`sidebar${sidebarAbierto ? ' open' : ''}`} role="navigation" aria-label="Menú principal">
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
            <NavLink key={item.to} to={item.to} onClick={() => setSidebarAbierto(false)} className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
              <i className={`ti ${item.icon}`} /> {item.label}
            </NavLink>
          ))}
        </div>

        <div className="sidebar-section">
          <div className="sidebar-label">Auditoría</div>
          {NAV_AUDITORIA.map((item) => (
            <NavLink key={item.to} to={item.to} onClick={() => setSidebarAbierto(false)} className={({ isActive }) => `nav-item${isActive ? ' active' : ''}`}>
              <i className={`ti ${item.icon}`} /> {item.label}
            </NavLink>
          ))}
        </div>

        <div className="sidebar-footer">
          <div className="avatar">{iniciales}</div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="user-name">{nombreUsuario}</div>
            <div className="user-role">{usuario?.rol === 'admin' ? 'Administrador' : usuario?.rol === 'supervisor' ? 'Supervisor' : 'Auditor certificado'}</div>
          </div>
          <button className="icon-btn-white" title="Cerrar sesión" aria-label="Cerrar sesión" onClick={cerrarSesion}>
            <i className="ti ti-logout" />
          </button>
        </div>
      </aside>

      <div className="main">
        <header className="topbar">
          <div className="topbar-left">
            <button className="sidebar-toggle" aria-label="Abrir menú" onClick={() => setSidebarAbierto(true)}>
              <i className="ti ti-menu-2" />
            </button>
            <button className="btn btn-sm" onClick={() => navigate('/')}>
              <i className="ti ti-arrow-left" /> <span className="btn-label">Volver al inicio</span>
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
              <i className="ti ti-plus" /> <span className="btn-label">Nueva empresa</span>
            </button>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => navigate(auditoriaActivaId ? '/auditor/formulario' : '/auditor/empresas')}
              title={auditoriaActivaId ? 'Continuar la auditoría activa' : 'Elige una empresa para iniciar una auditoría nueva'}
            >
              <i className="ti ti-player-play" /> <span className="btn-label">Iniciar auditoría</span>
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
