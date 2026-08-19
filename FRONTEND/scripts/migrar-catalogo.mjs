// ============================================================
// Migra el catálogo de auditoría (categorías → subcategorías →
// cuestionarios → preguntas) desde src/data/mockData.ts hacia la
// API real del backend (catalogo/*), en orden de dependencia.
// Idempotente por nombre: si una categoría/subcategoría/cuestionario
// con el mismo nombre ya existe, la reutiliza en vez de duplicarla
// (las preguntas si se agregan de nuevo si el cuestionario aún no
// tiene preguntas, para poder reintentar sin duplicar en un corte
// a mitad de camino).
//
// Uso:
//   API_URL=http://localhost:8080 AUDITOR_CORREO=auditor@prueba.com \
//   AUDITOR_PASSWORD=clave1234 node scripts/migrar-catalogo.mjs
// ============================================================

const API_URL = process.env.API_URL ?? 'http://localhost:8080';
const CORREO = process.env.AUDITOR_CORREO;
const PASSWORD = process.env.AUDITOR_PASSWORD;

if (!CORREO || !PASSWORD) {
  console.error('Faltan AUDITOR_CORREO / AUDITOR_PASSWORD en el entorno.');
  process.exit(1);
}

async function api(method, path, token, body) {
  const res = await fetch(`${API_URL}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    const texto = await res.text();
    throw new Error(`${method} ${path} -> ${res.status}: ${texto}`);
  }
  if (res.status === 204) return undefined;
  return res.json();
}

async function main() {
  const { categorias, subcategorias, cuestionarios, preguntasPorCuestionario } =
    await import('../src/data/mockData.ts');

  const login = await api('POST', '/api/auth/login', null, { correo: CORREO, password: PASSWORD });
  const token = login.accessToken;
  console.log('Login OK');

  const categoriasExistentes = await api('GET', '/api/categorias', token);
  const catPorNombre = new Map(categoriasExistentes.map((c) => [c.nombre, c]));

  const catIdMap = new Map();
  for (const cat of categorias) {
    let real = catPorNombre.get(cat.nombre);
    if (!real) {
      real = await api('POST', '/api/categorias', token, {
        nombre: cat.nombre,
        descripcion: cat.descripcion ?? null,
        icono: cat.icono ?? null,
        esPlantilla: true,
      });
      console.log('Categoria creada:', real.nombre);
    } else {
      console.log('Categoria reutilizada:', real.nombre);
    }
    catIdMap.set(cat.id, real.id);
  }

  const subIdMap = new Map();
  for (const sub of subcategorias) {
    const catRealId = catIdMap.get(sub.categoriaId);
    const existentes = await api('GET', `/api/categorias/${catRealId}/subcategorias`, token);
    let real = existentes.find((s) => s.nombre === sub.nombre);
    if (!real) {
      real = await api('POST', `/api/categorias/${catRealId}/subcategorias`, token, {
        nombre: sub.nombre,
        descripcion: sub.descripcion ?? null,
        responsable: sub.responsable ?? null,
        esPlantilla: true,
      });
      console.log('  Subcategoria creada:', real.nombre);
    } else {
      console.log('  Subcategoria reutilizada:', real.nombre);
    }
    subIdMap.set(sub.id, real.id);
  }

  const cuestIdMap = new Map();
  for (const cuest of cuestionarios) {
    const subRealId = subIdMap.get(cuest.subcategoriaId);
    const existentes = await api('GET', `/api/subcategorias/${subRealId}/cuestionarios`, token);
    let real = existentes.find((c) => c.nombre === cuest.nombre);
    if (!real) {
      real = await api('POST', `/api/subcategorias/${subRealId}/cuestionarios`, token, { nombre: cuest.nombre });
      console.log('    Cuestionario creado:', real.nombre);
    } else {
      console.log('    Cuestionario reutilizado:', real.nombre, '(', real.numPreguntas, 'preguntas )');
    }
    cuestIdMap.set(cuest.id, real.id);
  }

  let totalPreguntas = 0;
  for (const [cuestOldId, preguntas] of Object.entries(preguntasPorCuestionario)) {
    const cuestRealId = cuestIdMap.get(cuestOldId);
    const existentes = await api('GET', `/api/cuestionarios/${cuestRealId}/preguntas`, token);
    if (existentes.length > 0) {
      console.log(`      Preguntas de ${cuestOldId} ya cargadas (${existentes.length}), se omite.`);
      continue;
    }
    for (const p of preguntas) {
      await api('POST', `/api/cuestionarios/${cuestRealId}/preguntas`, token, {
        numero: p.numero,
        texto: p.texto,
        evidencia: p.evidencia ?? null,
      });
      totalPreguntas++;
    }
    console.log(`      ${preguntas.length} preguntas creadas para ${cuestOldId}`);
  }

  console.log(`\nListo. ${totalPreguntas} preguntas nuevas creadas.`);
}

main().catch((err) => {
  console.error('ERROR:', err.message);
  process.exit(1);
});
