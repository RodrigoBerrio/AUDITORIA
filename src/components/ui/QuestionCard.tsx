import type { Pregunta, ValorEscala } from '../../types/domain';
import { ScaleSelector } from './ScaleSelector';

interface Props {
  pregunta: Pregunta;
  totalPreguntas: number;
  valor?: ValorEscala;
  observacion?: string;
  onResponder: (valor: ValorEscala, observacion?: string) => void;
}

/**
 * Una sola pregunta del formulario de auditoría.
 * En el prototipo original este bloque estaba duplicado a mano
 * ~28 veces por cuestionario; aquí es un componente alimentado
 * por datos, así que agregar/editar preguntas es un cambio de
 * datos, no de markup.
 */
export function QuestionCard({ pregunta, totalPreguntas, valor, observacion, onResponder }: Props) {
  return (
    <div className="q-card">
      <div className="q-num">Pregunta {pregunta.numero} de {totalPreguntas}</div>
      <div className="q-text">{pregunta.texto}</div>
      {pregunta.evidencia && (
        <div className="q-evidence"><i className="ti ti-eye" /> Evidencia: {pregunta.evidencia}</div>
      )}
      <ScaleSelector value={valor} onChange={(v) => onResponder(v, observacion)} />
      <div className="q-obs">
        <input
          type="text"
          placeholder="Observación del auditor (opcional)…"
          aria-label="Observación"
          value={observacion ?? ''}
          onChange={(e) => onResponder(valor ?? (0 as ValorEscala), e.target.value)}
        />
      </div>
    </div>
  );
}
