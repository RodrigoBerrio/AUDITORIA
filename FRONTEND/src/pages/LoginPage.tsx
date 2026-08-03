import { useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppStore } from '../store/useAppStore';
import type { RolSesion } from '../store/useAppStore';
import { api, ApiError } from '../api/client';
import type { Usuario } from '../types/domain';

interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  usuario: Usuario;
}

/** Landing + selección de rol + login. Reemplaza showLogin()/backToRoles()/doLogin(). */
export function LoginPage() {
  const navigate = useNavigate();
  const iniciarSesionAuditor = useAppStore((s) => s.iniciarSesionAuditor);
  const iniciarSesionCliente = useAppStore((s) => s.iniciarSesionCliente);
  const [rolElegido, setRolElegido] = useState<RolSesion | null>(null);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);
  const emailRef = useRef<HTMLInputElement>(null);
  const passwordRef = useRef<HTMLInputElement>(null);

  const handleLogin = async () => {
    if (!rolElegido) return;

    // El rol 'cliente' sigue siendo una maqueta de UI — no hay backend para él en esta fase.
    if (rolElegido === 'cliente') {
      iniciarSesionCliente();
      navigate('/cliente/resumen');
      return;
    }

    // Se lee el valor real del input (no solo el estado de React): el
    // autocompletado de contraseñas del navegador a veces rellena el campo
    // visualmente sin disparar onChange, dejando el estado desincronizado.
    const correo = emailRef.current?.value.trim() || email.trim();
    const claveIngresada = passwordRef.current?.value || password;

    setError(null);
    setCargando(true);
    try {
      const respuesta = await api.post<LoginResponse>('/api/auth/login', { correo, password: claveIngresada });
      iniciarSesionAuditor(respuesta.usuario, respuesta.accessToken, respuesta.refreshToken);
      navigate('/auditor/dashboard');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'No se pudo conectar con el servidor.');
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="landing-page">
      <div className="landing-card">
        {!rolElegido ? (
          <div id="role-select">
            <div className="landing-shield"><i className="ti ti-shield-check" /></div>
            <div className="landing-brand">Auditorías Industriales SAS</div>
            <div className="landing-tagline">Plataforma de auditorías para PYMEs colombianas</div>

            <div className="landing-welcome">Gracias por visitar nuestra<br />plataforma de auditorías<br />empresariales</div>
            <div className="landing-prompt">Seleccione el tipo de usuario con el que<br />desea ingresar al sistema</div>

            <div className="role-cards">
              <div className="role-card" onClick={() => setRolElegido('cliente')}>
                <div className="role-icon ic-user"><i className="ti ti-building" /></div>
                <div>
                  <div className="role-label">Pulse aquí si es<br />usuario</div>
                  <div className="role-sub">(Cliente empresarial)</div>
                </div>
                <div className="role-arrow ic-user"><i className="ti ti-arrow-right" /></div>
              </div>
              <div className="role-card" onClick={() => setRolElegido('auditor')}>
                <div className="role-icon ic-admin"><i className="ti ti-shield-check" /></div>
                <div>
                  <div className="role-label" style={{ color: '#27AE60' }}>Pulse aquí si es<br />Administrador/Auditor</div>
                </div>
                <div className="role-arrow ic-admin"><i className="ti ti-arrow-right" /></div>
              </div>
            </div>
          </div>
        ) : (
          <div className="login-panel">
            <button className="login-back" onClick={() => setRolElegido(null)}>
              <i className="ti ti-arrow-left" /> Volver a la selección
            </button>
            <div className="login-role-badge" style={{ background: 'var(--brand-light)', color: 'var(--brand)' }}>
              {rolElegido === 'auditor' ? 'Administrador / Auditor' : 'Cliente empresarial'}
            </div>
            <div style={{ fontSize: 18, fontWeight: 700, color: 'var(--text-1)', marginBottom: 4 }}>Iniciar sesión</div>
            <div style={{ fontSize: 13, color: 'var(--text-3)', marginBottom: 20 }}>Ingresa tus credenciales para continuar</div>

            <div className="fg" style={{ marginBottom: 14 }}>
              <label className="lbl">Correo electrónico</label>
              <input ref={emailRef} className="inp" type="email" placeholder="correo@ejemplo.com" value={email} onChange={(e) => setEmail(e.target.value)} />
            </div>
            <div className="fg" style={{ marginBottom: 8 }}>
              <label className="lbl">Contraseña</label>
              <input
                ref={passwordRef}
                className="inp" type="password" placeholder="••••••••" value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleLogin(); }}
              />
            </div>
            <p style={{ textAlign: 'right', fontSize: 12, marginBottom: 20 }}>
              <a href="#" style={{ color: 'var(--brand-mid)', textDecoration: 'none' }}>¿Olvidaste tu contraseña?</a>
            </p>
            {error && (
              <div style={{ color: '#c0392b', fontSize: 13, marginBottom: 14 }}>{error}</div>
            )}
            <button
              className="btn btn-primary"
              style={{ width: '100%', justifyContent: 'center', padding: 11, fontSize: 14 }}
              onClick={handleLogin}
              disabled={cargando}
            >
              <i className="ti ti-login" /> {cargando ? 'Ingresando…' : 'Ingresar al sistema'}
            </button>
          </div>
        )}
      </div>
      <div className="landing-foot">
        Auditorías Industriales SAS v2.0 (React) — Uso exclusivo autorizado &nbsp;|&nbsp; <a href="#">Soporte técnico</a>
      </div>
    </div>
  );
}
