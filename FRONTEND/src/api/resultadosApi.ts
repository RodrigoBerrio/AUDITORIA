// ============================================================
// Endpoints agregados del módulo de resultados gráficos de
// auditoría (ResultadosController / EmpresaController#historico /
// ReporteController en el backend). El front nunca recalcula
// promedios ni umbrales de semáforo — todo llega ya agregado.
// ============================================================
import { api } from './client';
import type {
  HallazgosResumen, HistoricoPunto, RankingResultado, Reporte, ResumenAuditoria,
} from '../types/domain';

export const resultadosApi = {
  obtenerResumen: (auditoriaId: string, token?: string | null, meta?: number) =>
    api.get<ResumenAuditoria>(
      `/api/auditorias/${auditoriaId}/resumen${meta != null ? `?meta=${meta}` : ''}`, token,
    ),

  obtenerRanking: (auditoriaId: string, token?: string | null) =>
    api.get<RankingResultado>(`/api/auditorias/${auditoriaId}/ranking`, token),

  obtenerSeccionesCuestionario: (auditoriaId: string, cuestionarioId: string, token?: string | null) =>
    api.get<RankingResultado>(`/api/auditorias/${auditoriaId}/cuestionarios/${cuestionarioId}/secciones`, token),

  obtenerHallazgosResumen: (auditoriaId: string, token?: string | null) =>
    api.get<HallazgosResumen>(`/api/auditorias/${auditoriaId}/hallazgos/resumen`, token),

  obtenerHistorico: (empresaId: string, token?: string | null) =>
    api.get<HistoricoPunto[]>(`/api/empresas/${empresaId}/historico`, token),

  listarReportes: (auditoriaId: string, token?: string | null) =>
    api.get<Reporte[]>(`/api/auditorias/${auditoriaId}/reportes`, token),

  /**
   * Sin `alcance`: informe integral de siempre. Con `{ tipo: 'categoria' | 'subcategoria', nombre }`
   * (Etapa 5): informe independiente acotado a esa categoría/subcategoría — mismo endpoint.
   */
  generarReporte: (auditoriaId: string, token?: string | null, alcance?: { tipo: 'categoria' | 'subcategoria'; nombre: string }) => {
    const query = alcance ? `?${alcance.tipo}=${encodeURIComponent(alcance.nombre)}` : '';
    return api.post<Reporte>(`/api/auditorias/${auditoriaId}/reportes${query}`, {}, token);
  },
};
