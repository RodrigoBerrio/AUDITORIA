import { useAppStore } from '../../store/useAppStore';

/** Notificación flotante global — reemplaza showToast() del prototipo. */
export function ToastHost() {
  const { toast } = useAppStore();
  return (
    <div id="toast" className={`${toast.visible ? 'show ' : ''}${toast.tipo === 'warn' ? 't-warn' : 't-ok'}`} role="alert" aria-live="assertive">
      <i className={`ti ${toast.tipo === 'warn' ? 'ti-alert-triangle' : 'ti-check'}`} />
      <span>{toast.mensaje}</span>
    </div>
  );
}
