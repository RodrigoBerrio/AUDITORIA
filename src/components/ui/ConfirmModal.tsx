import { useAppStore } from '../../store/useAppStore';

/** Modal de confirmación global — reemplaza openConfirm/closeConfirm/doConfirm del prototipo. */
export function ConfirmModal() {
  const { confirm, cerrarConfirmacion } = useAppStore();

  const handleOk = () => {
    confirm.onConfirm?.();
    cerrarConfirmacion();
  };

  return (
    <div className={`overlay${confirm.open ? ' open' : ''}`} role="dialog" aria-modal="true">
      <div className="modal">
        <div className="modal-icon">⚠️</div>
        <div className="modal-title">{confirm.titulo}</div>
        <div className="modal-body">{confirm.cuerpo}</div>
        <div className="modal-actions">
          <button className="btn" onClick={cerrarConfirmacion}>Cancelar</button>
          <button className="btn btn-primary" onClick={handleOk}>{confirm.okLabel}</button>
        </div>
      </div>
    </div>
  );
}
