# Auditorías Industriales SAS — Frontend (React + TypeScript)

Migración del prototipo estático `auditapp.html` a una base en React,
pensada para escalar: componentes reutilizables, tipos alineados al
esquema Postgres v3, enrutamiento real y estado centralizado en vez de
variables globales y manipulación manual del DOM.

## Requisitos
- Node.js 18+

## Uso

```bash
npm install
npm run dev      # servidor de desarrollo
npm run build    # build de producción (tsc -b && vite build)
```

## Estructura

```
src/
  types/domain.ts        Tipos y enums derivados 1:1 de tablas.md (esquema v3)
  data/mockData.ts        Datos de ejemplo con la misma forma que tendrá la API real
  store/useAppStore.ts     Estado global (Zustand): sesión, auditoría en curso, toast, modal
  styles/theme.css         Tokens de diseño y clases de componentes portados del prototipo
  components/
    layout/                Sidebar + Topbar por rol (auditor / cliente)
    ui/                     ScaleSelector, QuestionCard, CategoryTree, ConfirmModal, ToastHost
  pages/
    LoginPage.tsx
    auditor/                Dashboard, Empresas, Categorías, Formulario de auditoría, Reportes
    cliente/                Resumen, Reportes, Hallazgos (portal del cliente empresarial)
  App.tsx                   Rutas: /auditor/* y /cliente/* protegidas por rol
```

## Próximos pasos sugeridos

1. Reemplazar `src/data/mockData.ts` por llamadas a la API real (misma forma de datos).
2. Conectar autenticación real en `LoginPage.tsx` (hoy solo simula el rol elegido).
3. Completar el catálogo de preguntas por cuestionario (`preguntasPorCuestionario`)
   a medida que el backend las sirva; `QuestionCard` ya es 100% data-driven.
4. Añadir pruebas (Vitest + Testing Library) antes de seguir creciendo el árbol de páginas.
