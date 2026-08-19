import { Navigate, Route, Routes } from 'react-router-dom';
import { useAppStore } from './store/useAppStore';
import { LoginPage } from './pages/LoginPage';
import { AuditorShell } from './components/layout/AuditorShell';
import { ClienteShell } from './components/layout/ClienteShell';
import { DashboardPage } from './pages/auditor/DashboardPage';
import { EmpresasPage } from './pages/auditor/EmpresasPage';
import { CategoriasPage } from './pages/auditor/CategoriasPage';
import { AuditoriaFormPage } from './pages/auditor/AuditoriaFormPage';
import { AuditoriaResultadosPage } from './pages/auditor/AuditoriaResultadosPage';
import { ReportesPage } from './pages/auditor/ReportesPage';
import { ClienteDashboardPage } from './pages/cliente/ClienteDashboardPage';
import { ClienteReportesPage } from './pages/cliente/ClienteReportesPage';
import { ClienteHallazgosPage } from './pages/cliente/ClienteHallazgosPage';

/** Protege un árbol de rutas exigiendo el rol correcto; si no, manda al login. */
function RutaProtegida({ rolRequerido, children }: { rolRequerido: 'auditor' | 'cliente'; children: React.ReactNode }) {
  const { rol, autenticado } = useAppStore();
  if (!autenticado || rol !== rolRequerido) return <Navigate to="/" replace />;
  return <>{children}</>;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />

      <Route
        path="/auditor"
        element={<RutaProtegida rolRequerido="auditor"><AuditorShell /></RutaProtegida>}
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="empresas" element={<EmpresasPage />} />
        <Route path="categorias" element={<CategoriasPage />} />
        <Route path="formulario" element={<AuditoriaFormPage />} />
        <Route path="auditorias/:id/resultados" element={<AuditoriaResultadosPage />} />
        <Route path="reportes" element={<ReportesPage />} />
      </Route>

      <Route
        path="/cliente"
        element={<RutaProtegida rolRequerido="cliente"><ClienteShell /></RutaProtegida>}
      >
        <Route index element={<Navigate to="resumen" replace />} />
        <Route path="resumen" element={<ClienteDashboardPage />} />
        <Route path="reportes" element={<ClienteReportesPage />} />
        <Route path="hallazgos" element={<ClienteHallazgosPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
