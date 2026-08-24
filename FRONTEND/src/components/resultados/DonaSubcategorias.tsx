import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts';
import { ChartContainer } from './ChartContainer';
import type { PuntajeSubcategoria } from '../../types/domain';

interface DonaSubcategoriasProps {
  subcategorias: PuntajeSubcategoria[];
}

/**
 * Dona (nunca torta) con la calificación de cada subcategoría evaluada — reemplaza el radar general
 * por categoría cuando hay pocas categorías (un radar de 1 eje no dice nada). El color de cada porción
 * usa el mismo semáforo de 3 niveles que el resto del dashboard (regla no negociable), no colores libres,
 * así que las subcategorías del mismo nivel de madurez comparten color — la leyenda distingue cuál es cuál.
 */
export function DonaSubcategorias({ subcategorias }: DonaSubcategoriasProps) {
  const datos = subcategorias.filter((s) => s.evaluada && s.puntaje != null);
  const vacio = datos.length === 0;

  const resumen = datos
    .map((s) => `${s.subcategoria}: ${s.puntaje!.toFixed(1)} de 5.0`)
    .join('; ');

  return (
    <ChartContainer
      titulo="Distribución de calificaciones por subcategoría"
      subtitulo="Auditoría integral — color según nivel de madurez"
      ariaLabel={`Distribución de calificaciones por subcategoría. ${resumen}`}
      vacio={vacio}
      mensajeVacio="Todavía no hay subcategorías evaluadas."
      fallbackTexto={
        <ul style={{ paddingLeft: 18 }}>
          {datos.map((s) => (
            <li key={s.subcategoria}>{s.subcategoria}: {s.puntaje!.toFixed(1)} / 5.0</li>
          ))}
        </ul>
      }
    >
      <ResponsiveContainer width="100%" height={300}>
        <PieChart>
          <Pie data={datos} dataKey="puntaje" nameKey="subcategoria" innerRadius="55%" outerRadius="80%" paddingAngle={2} isAnimationActive={false}>
            {datos.map((s) => (
              <Cell key={s.subcategoria} fill={s.colorSemaforo} stroke="var(--surface)" strokeWidth={2} />
            ))}
          </Pie>
          <Tooltip
            formatter={(valor, _nombre, item) => {
              const payload = (item as unknown as { payload: PuntajeSubcategoria }).payload;
              return [`${Number(valor).toFixed(1)} / 5.0`, payload.subcategoria];
            }}
          />
          <Legend
            formatter={(_valor, entry) => {
              const payload = (entry as unknown as { payload: PuntajeSubcategoria }).payload;
              return `${payload.subcategoria} — ${payload.puntaje!.toFixed(1)} / 5.0`;
            }}
            wrapperStyle={{ fontSize: 12 }}
          />
        </PieChart>
      </ResponsiveContainer>
    </ChartContainer>
  );
}
