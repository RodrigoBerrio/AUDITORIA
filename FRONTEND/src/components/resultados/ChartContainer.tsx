import type { ReactNode } from 'react';

interface ChartContainerProps {
  titulo: string;
  subtitulo?: string;
  /** Resumen legible por lectores de pantalla, ej. "Radar de madurez: Diagnóstico energético 4.2, Cultura 2.1 sobre 5". */
  ariaLabel: string;
  vacio?: boolean;
  mensajeVacio?: string;
  /** Lista de datos en texto plano — visible bajo <details>, no solo sr-only (regla de accesibilidad del spec). */
  fallbackTexto?: ReactNode;
  headerExtra?: ReactNode;
  children: ReactNode;
}

/** Envoltorio común de accesibilidad/estado-vacío para las 6 gráficas de resultados: nunca un gráfico en blanco o sin fallback textual. */
export function ChartContainer({ titulo, subtitulo, ariaLabel, vacio, mensajeVacio, fallbackTexto, headerExtra, children }: ChartContainerProps) {
  return (
    <div className="card" style={{ marginBottom: 0 }}>
      <div className="card-hd">
        <div>
          <div className="card-title">{titulo}</div>
          {subtitulo && <div className="card-sub">{subtitulo}</div>}
        </div>
        {headerExtra}
      </div>

      {vacio ? (
        <div className="empty">
          <i className="ti ti-chart-bar-off" />
          <div className="empty-t">Sin datos suficientes</div>
          <div className="empty-s">{mensajeVacio ?? 'Todavía no hay información para mostrar esta gráfica.'}</div>
        </div>
      ) : (
        <>
          <div role="img" aria-label={ariaLabel}>
            {children}
          </div>
          {fallbackTexto && (
            <details style={{ marginTop: 12 }}>
              <summary style={{ fontSize: 12, color: 'var(--text-3)', cursor: 'pointer' }}>Ver datos en texto</summary>
              <div style={{ marginTop: 8, fontSize: 12.5, color: 'var(--text-2)' }}>{fallbackTexto}</div>
            </details>
          )}
        </>
      )}
    </div>
  );
}
