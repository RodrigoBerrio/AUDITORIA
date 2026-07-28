import { useState } from 'react';
import type { Categoria, Subcategoria } from '../../types/domain';

interface Props {
  categorias: Categoria[];
  subcategoriasPorCategoria: Record<string, Subcategoria[]>;
  seleccionado: { tipo: 'categoria' | 'subcategoria'; id: string } | null;
  onSeleccionar: (nodo: { tipo: 'categoria' | 'subcategoria'; id: string; categoriaId?: string }) => void;
  onAgregarCategoria: (nombre: string) => void;
}

/**
 * Árbol categoría → subcategoría.
 * Sustituye renderTree()/makeSubNode()/selTreeNode() del prototipo
 * (creación manual de nodos con document.createElement) por estado
 * de React: expandir/colapsar y seleccionar ya no tocan el DOM a mano.
 */
export function CategoryTree({ categorias, subcategoriasPorCategoria, seleccionado, onSeleccionar, onAgregarCategoria }: Props) {
  const [expandido, setExpandido] = useState<Record<string, boolean>>({});

  const toggle = (id: string) => setExpandido((prev) => ({ ...prev, [id]: !prev[id] }));

  const agregarCategoriaRaiz = () => {
    const nombre = window.prompt('Nombre de la nueva categoría raíz:');
    if (nombre && nombre.trim()) onAgregarCategoria(nombre.trim());
  };

  return (
    <div className="tree" id="dyn-tree">
      {categorias.map((cat) => {
        const hijos = subcategoriasPorCategoria[cat.id] ?? [];
        const abierto = expandido[cat.id] ?? true;
        const catSeleccionada = seleccionado?.tipo === 'categoria' && seleccionado.id === cat.id;
        return (
          <div key={cat.id}>
            <div
              className={`tree-row${catSeleccionada ? ' sel' : ''}`}
              onClick={() => { toggle(cat.id); onSeleccionar({ tipo: 'categoria', id: cat.id }); }}
            >
              <i className={`ti ${abierto ? 'ti-folder-open' : 'ti-folder'}`} />
              {cat.nombre}
            </div>
            {abierto && (
              <div className="tree-children">
                {hijos.map((sub) => {
                  const subSeleccionada = seleccionado?.tipo === 'subcategoria' && seleccionado.id === sub.id;
                  return (
                    <div
                      key={sub.id}
                      className={`tree-child${subSeleccionada ? ' sel' : ''}`}
                      onClick={() => onSeleccionar({ tipo: 'subcategoria', id: sub.id, categoriaId: cat.id })}
                    >
                      <i className="ti ti-file-text" />
                      {sub.nombre}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        );
      })}
      <button className="btn btn-sm" style={{ marginTop: 10 }} onClick={agregarCategoriaRaiz}>
        <i className="ti ti-plus" /> Nueva categoría raíz
      </button>
    </div>
  );
}
