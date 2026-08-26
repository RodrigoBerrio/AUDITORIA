/** Parte una etiqueta larga en varias líneas (~13 caracteres) sin cortar palabras. */
function partirEnLineas(texto: string, maxCaracteres = 13): string[] {
  const palabras = texto.split(' ');
  const lineas: string[] = [];
  let actual = '';
  for (const palabra of palabras) {
    const candidato = actual ? `${actual} ${palabra}` : palabra;
    if (candidato.length > maxCaracteres && actual) {
      lineas.push(actual);
      actual = palabra;
    } else {
      actual = candidato;
    }
  }
  if (actual) lineas.push(actual);
  return lineas;
}

interface TickEtiquetaLargaProps {
  x?: number | string;
  y?: number | string;
  payload?: { value: string };
  textAnchor?: 'inherit' | 'start' | 'middle' | 'end';
}

/** Tick multilínea para ejes de radar (PolarAngleAxis) — evita que etiquetas largas (nombres de
 * subcategoría o categoría) se corten contra el borde del gráfico o se encimen entre sí. */
export function TickEtiquetaLarga({ x, y, payload, textAnchor }: TickEtiquetaLargaProps) {
  if (!payload) return null;
  const lineas = partirEnLineas(payload.value);
  const offsetInicial = -((lineas.length - 1) * 5.5);
  return (
    <text x={x} y={y} textAnchor={textAnchor} fontSize={10.5} fill="var(--text-2)">
      {lineas.map((linea, i) => (
        <tspan key={i} x={x} dy={i === 0 ? offsetInicial : 11}>{linea}</tspan>
      ))}
    </text>
  );
}
