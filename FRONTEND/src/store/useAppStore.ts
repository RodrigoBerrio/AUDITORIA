// ============================================================
// Estado compartido de la aplicación.
// Reemplaza las variables globales sueltas del prototipo
// (currentRole, answered, activeQuestId, newCatSubcatRows...)
// por un único store predecible con Zustand.
// ============================================================
import { create } from 'zustand';
import type { EvidenciaFotografica, Usuario, ValorEscala } from '../types/domain';

export type RolSesion = 'auditor' | 'cliente';

interface RespuestaSesion {
  valor: ValorEscala;
  observacion?: string;
}

interface ToastState {
  visible: boolean;
  mensaje: string;
  tipo: 'ok' | 'warn';
}

interface ConfirmState {
  open: boolean;
  titulo: string;
  cuerpo: string;
  okLabel: string;
  onConfirm: (() => void) | null;
}

interface AppState {
  // Sesión
  rol: RolSesion | null;
  autenticado: boolean;
  usuario: Usuario | null;
  accessToken: string | null;
  refreshToken: string | null;
  /** Login real contra el backend (POST /api/auth/login); rol 'cliente' no existe en esta fase. */
  iniciarSesionAuditor: (usuario: Usuario, accessToken: string, refreshToken: string) => void;
  /** rol 'cliente' sigue siendo una simulación de UI — fuera de alcance, sin backend real. */
  iniciarSesionCliente: () => void;
  logout: () => void;

  // Auditoría en curso (sesión de trabajo del auditor)
  empresaActivaId: string | null;
  auditoriaActivaId: string | null;
  cuestionarioActivoId: string | null;
  respuestas: Record<string, RespuestaSesion>;
  evidenciasPorPregunta: Record<string, EvidenciaFotografica[]>;
  setEmpresaActiva: (empresaId: string) => void;
  setAuditoriaActiva: (auditoriaId: string | null) => void;
  setCuestionarioActivo: (cuestionarioId: string) => void;
  responder: (preguntaId: string, valor: ValorEscala, observacion?: string) => void;
  agregarEvidencia: (preguntaId: string, evidencia: EvidenciaFotografica) => void;
  reiniciarSesionAuditoria: () => void;

  // Toast
  toast: ToastState;
  mostrarToast: (mensaje: string, tipo?: 'ok' | 'warn') => void;
  ocultarToast: () => void;

  // Modal de confirmación
  confirm: ConfirmState;
  pedirConfirmacion: (titulo: string, cuerpo: string, okLabel: string, onConfirm: () => void) => void;
  cerrarConfirmacion: () => void;
}

let toastTimer: ReturnType<typeof setTimeout> | undefined;

export const useAppStore = create<AppState>((set, get) => ({
  rol: null,
  autenticado: false,
  usuario: null,
  accessToken: null,
  refreshToken: null,
  iniciarSesionAuditor: (usuario, accessToken, refreshToken) =>
    set({ rol: 'auditor', autenticado: true, usuario, accessToken, refreshToken }),
  iniciarSesionCliente: () => set({ rol: 'cliente', autenticado: true, usuario: null, accessToken: null, refreshToken: null }),
  logout: () => set({ rol: null, autenticado: false, usuario: null, accessToken: null, refreshToken: null }),

  empresaActivaId: null,
  auditoriaActivaId: null,
  cuestionarioActivoId: null,
  respuestas: {},
  evidenciasPorPregunta: {},
  setEmpresaActiva: (empresaId) => set({ empresaActivaId: empresaId }),
  setAuditoriaActiva: (auditoriaId) => set({ auditoriaActivaId: auditoriaId }),
  setCuestionarioActivo: (cuestionarioId) => set({ cuestionarioActivoId: cuestionarioId }),
  responder: (preguntaId, valor, observacion) =>
    set((state) => ({
      respuestas: {
        ...state.respuestas,
        [preguntaId]: { valor, observacion: observacion ?? state.respuestas[preguntaId]?.observacion },
      },
    })),
  agregarEvidencia: (preguntaId, evidencia) =>
    set((state) => ({
      evidenciasPorPregunta: {
        ...state.evidenciasPorPregunta,
        [preguntaId]: [...(state.evidenciasPorPregunta[preguntaId] ?? []), evidencia],
      },
    })),
  reiniciarSesionAuditoria: () => set({ respuestas: {}, cuestionarioActivoId: null, evidenciasPorPregunta: {}, auditoriaActivaId: null }),

  toast: { visible: false, mensaje: '', tipo: 'ok' },
  mostrarToast: (mensaje, tipo = 'ok') => {
    clearTimeout(toastTimer);
    set({ toast: { visible: true, mensaje, tipo } });
    toastTimer = setTimeout(() => get().ocultarToast(), 2600);
  },
  ocultarToast: () => set((state) => ({ toast: { ...state.toast, visible: false } })),

  confirm: { open: false, titulo: '', cuerpo: '', okLabel: 'Confirmar', onConfirm: null },
  pedirConfirmacion: (titulo, cuerpo, okLabel, onConfirm) =>
    set({ confirm: { open: true, titulo, cuerpo, okLabel, onConfirm } }),
  cerrarConfirmacion: () =>
    set((state) => ({ confirm: { ...state.confirm, open: false } })),
}));

/** Selector derivado: promedio y progreso de un conjunto de preguntas. */
export function useProgresoCuestionario(preguntaIds: string[]) {
  const respuestas = useAppStore((s) => s.respuestas);
  const respondidas = preguntaIds.filter((id) => respuestas[id] !== undefined);
  const promedio = respondidas.length
    ? respondidas.reduce((sum, id) => sum + respuestas[id].valor, 0) / respondidas.length
    : null;
  return {
    total: preguntaIds.length,
    respondidas: respondidas.length,
    promedio: promedio ? Math.round(promedio * 10) / 10 : null,
  };
}
