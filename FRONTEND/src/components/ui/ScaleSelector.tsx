import { ESCALA_MADUREZ } from '../../types/domain';
import type { ValorEscala } from '../../types/domain';

interface Props {
  value?: ValorEscala;
  onChange: (valor: ValorEscala) => void;
}

/** Escala de madurez 1-5, reutilizable en cualquier pregunta del formulario. */
export function ScaleSelector({ value, onChange }: Props) {
  return (
    <>
      <div className="scale-opts">
        {ESCALA_MADUREZ.map((op) => (
          <button
            key={op.valor}
            type="button"
            className={`scale-opt${value === op.valor ? ' sel' : ''}`}
            data-v={op.valor}
            data-tip={op.descripcion}
            onClick={() => onChange(op.valor)}
          >
            {op.valor}
            <span className="scale-opt-word">{op.etiqueta}</span>
          </button>
        ))}
      </div>
      <div className="scale-labels">
        <span>Básico/reactivo</span>
        <span>Avanzado/analítico</span>
      </div>
    </>
  );
}
