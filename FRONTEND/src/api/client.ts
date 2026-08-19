// ============================================================
// Cliente HTTP mínimo hacia el backend real (fetch nativo, sin
// axios — coherente con package.json). Reemplaza progresivamente
// las lecturas de mockData.ts pantalla por pantalla.
// ============================================================
import { useAppStore } from '../store/useAppStore';

const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

interface ParTokens {
  accessToken: string;
  refreshToken: string;
}

// El access token dura 15 min (JWT_ACCESS_TOKEN_MINUTES); deduplicado para que varias
// peticiones que expiran a la vez no disparen cada una su propio /api/auth/refresh.
let refrescoEnCurso: Promise<ParTokens | null> | null = null;

function refrescarToken(refreshToken: string): Promise<ParTokens | null> {
  if (!refrescoEnCurso) {
    refrescoEnCurso = fetch(`${API_URL}/api/auth/refresh`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    })
      .then((res) => (res.ok ? res.json() : null))
      .then((body) => (body ? { accessToken: body.accessToken, refreshToken: body.refreshToken } : null))
      .catch(() => null)
      .finally(() => {
        refrescoEnCurso = null;
      });
  }
  return refrescoEnCurso;
}

async function request<T>(path: string, options: RequestInit, token?: string | null, isMultipart = false, reintentado = false): Promise<T> {
  const headers: HeadersInit = {
    // El boundary del multipart lo fija el navegador; fijar Content-Type a mano lo rompe.
    ...(isMultipart ? {} : { 'Content-Type': 'application/json' }),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const res = await fetch(`${API_URL}${path}`, { ...options, headers });

  // Access token expirado: intenta renovarlo una sola vez con el refresh token y repite la
  // petición original antes de rendirse (evita el 401 "Se requiere autenticación" en medio de
  // una sesión de trabajo válida — /api/auth/* queda fuera para no reintentar el propio login/refresh).
  if (res.status === 401 && !reintentado && !path.startsWith('/api/auth/')) {
    const { refreshToken, usuario } = useAppStore.getState();
    if (refreshToken && usuario) {
      const nuevos = await refrescarToken(refreshToken);
      if (nuevos) {
        useAppStore.getState().iniciarSesionAuditor(usuario, nuevos.accessToken, nuevos.refreshToken);
        return request<T>(path, options, nuevos.accessToken, isMultipart, true);
      }
    }
    useAppStore.getState().logout();
  }

  if (!res.ok) {
    let mensaje = `Error ${res.status}`;
    try {
      const cuerpo = await res.json();
      mensaje = cuerpo.message ?? mensaje;
    } catch {
      // respuesta de error sin cuerpo JSON — se conserva el mensaje genérico
    }
    throw new ApiError(res.status, mensaje);
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

export const api = {
  get: <T>(path: string, token?: string | null) => request<T>(path, { method: 'GET' }, token),
  post: <T>(path: string, body: unknown, token?: string | null) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }, token),
  put: <T>(path: string, body: unknown, token?: string | null) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }, token),
  delete: <T>(path: string, token?: string | null) => request<T>(path, { method: 'DELETE' }, token),
  postMultipart: <T>(path: string, formData: FormData, token?: string | null) =>
    request<T>(path, { method: 'POST', body: formData }, token, true),
};
