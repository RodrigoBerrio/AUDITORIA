package com.auditoriaindustriales.bakend.resultados.application;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaCuestionarioResponse;
import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaResponse;
import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoResponse;
import com.auditoriaindustriales.bakend.auditorias.api.dto.RespuestaResponse;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaCuestionarioService;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaService;
import com.auditoriaindustriales.bakend.auditorias.application.HallazgoService;
import com.auditoriaindustriales.bakend.auditorias.application.RespuestaService;
import com.auditoriaindustriales.bakend.auditorias.domain.EstadoHallazgo;
import com.auditoriaindustriales.bakend.auditorias.domain.Severidad;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CategoriaResponse;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CuestionarioResponse;
import com.auditoriaindustriales.bakend.catalogo.api.dto.SubcategoriaResponse;
import com.auditoriaindustriales.bakend.catalogo.application.CategoriaService;
import com.auditoriaindustriales.bakend.catalogo.application.CuestionarioService;
import com.auditoriaindustriales.bakend.catalogo.application.SubcategoriaService;
import com.auditoriaindustriales.bakend.catalogo.domain.Pregunta;
import com.auditoriaindustriales.bakend.catalogo.domain.PreguntaRepository;
import com.auditoriaindustriales.bakend.reportes.domain.NivelMadurez;
import com.auditoriaindustriales.bakend.resultados.api.dto.HallazgosResumenResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.ItemPuntaje;
import com.auditoriaindustriales.bakend.resultados.api.dto.RankingResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.ResumenAuditoriaResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.SeccionesResponse;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import com.auditoriaindustriales.bakend.shared.domain.Semaforo;
import com.auditoriaindustriales.bakend.shared.domain.SemaforoDetalle;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Read-model de agregación para el módulo de resultados gráficos: no tiene
 * entidades ni tabla propia, solo compone datos ya calculados por los demás
 * módulos (los triggers de Postgres ya recalculan puntaje/puntaje_global,
 * así que aquí nunca se reescriben esos dos valores) y aplica el semáforo
 * único (shared.domain.Semaforo) para que KPI, radar, ranking, dona y el PDF
 * coincidan siempre en el mismo color para el mismo dato.
 */
@Service
@Transactional(readOnly = true)
public class ResultadosService {

    private static final String SECCION_DEFECTO = "General";
    static final BigDecimal META_DEFECTO = new BigDecimal("4.0");

    private final AuditoriaService auditoriaService;
    private final AuditoriaCuestionarioService auditoriaCuestionarioService;
    private final CategoriaService categoriaService;
    private final CuestionarioService cuestionarioService;
    private final SubcategoriaService subcategoriaService;
    private final RespuestaService respuestaService;
    private final PreguntaRepository preguntaRepository;
    private final HallazgoService hallazgoService;

    public ResultadosService(
            AuditoriaService auditoriaService,
            AuditoriaCuestionarioService auditoriaCuestionarioService,
            CategoriaService categoriaService,
            CuestionarioService cuestionarioService,
            SubcategoriaService subcategoriaService,
            RespuestaService respuestaService,
            PreguntaRepository preguntaRepository,
            HallazgoService hallazgoService) {
        this.auditoriaService = auditoriaService;
        this.auditoriaCuestionarioService = auditoriaCuestionarioService;
        this.categoriaService = categoriaService;
        this.cuestionarioService = cuestionarioService;
        this.subcategoriaService = subcategoriaService;
        this.respuestaService = respuestaService;
        this.preguntaRepository = preguntaRepository;
        this.hallazgoService = hallazgoService;
    }

    public ResumenAuditoriaResponse resumen(UUID auditoriaId, BigDecimal meta) {
        AuditoriaResponse auditoria = auditoriaService.obtener(auditoriaId);
        BigDecimal metaEfectiva = meta != null ? meta : META_DEFECTO;
        List<DatoSubcategoria> datos = datosSubcategorias(auditoriaId);

        List<ResumenAuditoriaResponse.PuntajeSubcategoria> subcategorias = datos.stream()
                .map(d -> new ResumenAuditoriaResponse.PuntajeSubcategoria(
                        d.subcategoria(), d.categoria(), d.puntaje(), metaEfectiva, colorSemaforo(d.puntaje()), d.evaluada(), d.detalle(), d.enAlcance()))
                .toList();

        Map<String, List<DatoSubcategoria>> porCategoria = datos.stream()
                .collect(Collectors.groupingBy(DatoSubcategoria::categoria, LinkedHashMap::new, Collectors.toList()));

        List<ResumenAuditoriaResponse.PuntajeCategoria> categorias = porCategoria.entrySet().stream()
                .map(entrada -> {
                    List<DatoSubcategoria> subs = entrada.getValue();
                    long evaluadas = subs.stream().filter(DatoSubcategoria::evaluada).count();
                    long enAlcance = subs.stream().filter(DatoSubcategoria::enAlcance).count();
                    boolean completa = evaluadas == subs.size();
                    BigDecimal puntajeCategoria = completa
                            ? promedioBigDecimal(subs.stream().map(DatoSubcategoria::puntaje).filter(Objects::nonNull).toList())
                            : null;
                    return new ResumenAuditoriaResponse.PuntajeCategoria(
                            entrada.getKey(), puntajeCategoria, colorSemaforo(puntajeCategoria), completa, (int) evaluadas, subs.size(), (int) enAlcance);
                })
                .toList();

        boolean catalogoCompleto = !categorias.isEmpty()
                && categorias.stream().allMatch(ResumenAuditoriaResponse.PuntajeCategoria::completa);
        long subcategoriasEvaluadas = datos.stream().filter(DatoSubcategoria::evaluada).count();
        long subcategoriasEnAlcance = datos.stream().filter(DatoSubcategoria::enAlcance).count();

        String nivelMadurez = auditoria.puntajeGlobal() != null ? NivelMadurez.desde(auditoria.puntajeGlobal()).getEtiqueta() : null;
        Progreso progreso = progresoPreguntas(auditoriaCuestionarioService.listarPorAuditoria(auditoriaId));

        return new ResumenAuditoriaResponse(
                auditoria.puntajeGlobal(), nivelMadurez, Semaforo.desde(auditoria.puntajeGlobal()).getColorHex(),
                subcategorias, progreso.respondidas(), progreso.esperadas(),
                categorias, catalogoCompleto, (int) subcategoriasEvaluadas, datos.size(), (int) subcategoriasEnAlcance);
    }

    public RankingResponse ranking(UUID auditoriaId) {
        List<DatoSubcategoria> datos = datosSubcategorias(auditoriaId);

        List<ItemPuntaje> items = datos.stream()
                .sorted(Comparator.comparing(d -> ordenable(d.puntaje())))
                .map(d -> new ItemPuntaje(d.subcategoria(), d.puntaje(), colorSemaforo(d.puntaje()), d.evaluada()))
                .toList();

        Progreso progreso = progresoPreguntas(auditoriaCuestionarioService.listarPorAuditoria(auditoriaId));
        return new RankingResponse(items, progreso.respondidas(), progreso.esperadas());
    }

    /**
     * Recorre TODO el catálogo activo (no solo lo aplicado a la auditoría) para que las
     * subcategorías todavía no evaluadas aparezcan como hueco pendiente (plano cartesiano
     * vacío) en vez de desaparecer de la lista. Una subcategoría queda "evaluada" solo cuando
     * TODOS sus cuestionarios activos están aplicados a la auditoría y 100% respondidos.
     */
    private List<DatoSubcategoria> datosSubcategorias(UUID auditoriaId) {
        Map<UUID, AuditoriaCuestionarioResponse> aplicadosPorCuestionario = auditoriaCuestionarioService.listarPorAuditoria(auditoriaId)
                .stream()
                .collect(Collectors.toMap(AuditoriaCuestionarioResponse::cuestionarioId, ac -> ac));

        List<DatoSubcategoria> resultado = new ArrayList<>();
        for (CategoriaResponse categoria : categoriaService.listar(true)) {
            for (SubcategoriaResponse sub : subcategoriaService.listarPorCategoria(categoria.id(), true)) {
                List<CuestionarioResponse> cuestionarios = cuestionarioService.listarPorSubcategoria(sub.id(), true);
                if (cuestionarios.isEmpty()) {
                    continue;
                }
                boolean evaluada = true;
                boolean enAlcance = false;
                List<BigDecimal> puntajesCuestionarios = new ArrayList<>();
                List<ItemPuntaje> detalle = new ArrayList<>();
                boolean prefijarConCuestionario = cuestionarios.size() > 1;
                for (CuestionarioResponse c : cuestionarios) {
                    AuditoriaCuestionarioResponse ac = aplicadosPorCuestionario.get(c.id());
                    if (ac != null) {
                        enAlcance = true;
                    }
                    int respondidas = ac != null ? respuestaService.listarPorAuditoriaCuestionario(ac.id()).size() : 0;
                    boolean completo = c.numPreguntas() > 0 && ac != null && respondidas == c.numPreguntas();
                    if (!completo) {
                        evaluada = false;
                    } else {
                        if (ac.puntaje() != null) {
                            puntajesCuestionarios.add(ac.puntaje());
                        }
                        detalle.addAll(detalleCuestionario(ac, c.nombre(), prefijarConCuestionario));
                    }
                }
                BigDecimal puntaje = evaluada ? promedioBigDecimal(puntajesCuestionarios) : null;
                List<ItemPuntaje> detalleFinal = evaluada
                        ? detalle.stream().sorted(Comparator.comparing(item -> ordenable(item.puntaje()))).toList()
                        : List.of();
                resultado.add(new DatoSubcategoria(sub.nombre(), categoria.nombre(), puntaje, evaluada, detalleFinal, enAlcance));
            }
        }
        return resultado;
    }

    private static String colorSemaforo(BigDecimal puntaje) {
        return Semaforo.desde(puntaje).getColorHex();
    }

    /**
     * Una subcategoría del catálogo con su estado de evaluación dentro de una auditoría puntual
     * (no una entidad de dominio). "Alcance" inferido, no declarado: enAlcance=true en cuanto se
     * aplicó al menos uno de sus cuestionarios, aunque todavía no esté completo — distingue "no
     * iniciada" de "en progreso", ambas antes indistinguibles bajo evaluada=false.
     */
    private record DatoSubcategoria(String subcategoria, String categoria, BigDecimal puntaje, boolean evaluada, List<ItemPuntaje> detalle, boolean enAlcance) {
    }

    public SeccionesResponse secciones(UUID auditoriaId, UUID cuestionarioId) {
        AuditoriaCuestionarioResponse aplicado = auditoriaCuestionarioService.listarPorAuditoria(auditoriaId).stream()
                .filter(ac -> ac.cuestionarioId().equals(cuestionarioId))
                .findFirst()
                .orElseThrow(() -> NotFoundException.of("Cuestionario aplicado a la auditoría", cuestionarioId));

        List<ItemPuntaje> items = detalleCuestionario(aplicado, null, false).stream()
                .sorted(Comparator.comparing(item -> ordenable(item.puntaje())))
                .toList();

        int esperadas = cuestionarioService.obtener(cuestionarioId).numPreguntas();
        int respondidas = respuestaService.listarPorAuditoriaCuestionario(aplicado.id()).size();
        return new SeccionesResponse(items, respondidas, esperadas);
    }

    /**
     * Desglose de un cuestionario aplicado, agrupado por la "sección" de cada pregunta (o un solo
     * bloque "General" si el cuestionario no tiene secciones etiquetadas — la mayoría del catálogo
     * actual). Reutilizado tanto por el endpoint público de secciones (un cuestionario a la vez,
     * elegido en un desplegable) como por el detalle por subcategoría del reporte (todas las
     * subcategorías de una categoría a la vez, sin selección manual).
     *
     * @param nombreCuestionario etiqueta a usar cuando el cuestionario no tiene secciones propias.
     * @param prefijarSiGeneral  si true y el resultado es un único bloque "General", usa
     *                           nombreCuestionario como etiqueta de la barra en vez de "General" —
     *                           necesario cuando una subcategoría agrupa más de un cuestionario y
     *                           dos bloques "General" serían indistinguibles en el mismo gráfico.
     */
    private List<ItemPuntaje> detalleCuestionario(AuditoriaCuestionarioResponse aplicado, String nombreCuestionario, boolean prefijarSiGeneral) {
        List<RespuestaResponse> respuestas = respuestaService.listarPorAuditoriaCuestionario(aplicado.id());

        Map<String, List<Integer>> valoresPorSeccion = new LinkedHashMap<>();
        for (RespuestaResponse respuesta : respuestas) {
            String seccion = preguntaRepository.buscarPorId(respuesta.preguntaId())
                    .map(Pregunta::getSeccion)
                    .filter(s -> s != null && !s.isBlank())
                    .orElse(SECCION_DEFECTO);
            valoresPorSeccion.computeIfAbsent(seccion, s -> new ArrayList<>()).add(respuesta.valor());
        }

        boolean unSoloBloqueGeneral = prefijarSiGeneral && nombreCuestionario != null
                && valoresPorSeccion.size() == 1 && valoresPorSeccion.containsKey(SECCION_DEFECTO);

        return valoresPorSeccion.entrySet().stream()
                .map(entrada -> {
                    BigDecimal promedio = promedio(entrada.getValue());
                    String etiqueta = unSoloBloqueGeneral ? nombreCuestionario : entrada.getKey();
                    // Nivel de criterio/dimensión: escala de 4 pasos (SemaforoDetalle), no el
                    // semáforo de 3 pasos de subcategoría/categoría/auditoría (Semaforo) — misma
                    // fuente que antes solo vivía en el frontend, ahora también en el PDF.
                    return new ItemPuntaje(etiqueta, promedio, SemaforoDetalle.desde(promedio).getColorHex(), true);
                })
                .toList();
    }

    public HallazgosResumenResponse hallazgosResumen(UUID auditoriaId) {
        List<HallazgoResponse> hallazgos = hallazgoService.listarPorAuditoria(auditoriaId);
        int total = hallazgos.size();

        List<HallazgosResumenResponse.ConteoSeveridad> porSeveridad = new java.util.ArrayList<>();
        for (Severidad severidad : Severidad.values()) {
            long cantidad = hallazgos.stream().filter(h -> h.severidad() == severidad).count();
            porSeveridad.add(new HallazgosResumenResponse.ConteoSeveridad(
                    severidad, (int) cantidad, porcentaje(cantidad, total), severidad.colorHex()));
        }

        List<HallazgosResumenResponse.ConteoEstado> porEstado = new java.util.ArrayList<>();
        for (EstadoHallazgo estado : EstadoHallazgo.values()) {
            long cantidad = hallazgos.stream().filter(h -> h.estado() == estado).count();
            porEstado.add(new HallazgosResumenResponse.ConteoEstado(estado, (int) cantidad, porcentaje(cantidad, total)));
        }

        return new HallazgosResumenResponse(total, porSeveridad, porEstado);
    }

    private Progreso progresoPreguntas(List<AuditoriaCuestionarioResponse> aplicados) {
        int respondidas = 0;
        int esperadas = 0;
        for (AuditoriaCuestionarioResponse ac : aplicados) {
            respondidas += respuestaService.listarPorAuditoriaCuestionario(ac.id()).size();
            esperadas += cuestionarioService.obtener(ac.cuestionarioId()).numPreguntas();
        }
        return new Progreso(respondidas, esperadas);
    }

    private static BigDecimal promedio(List<Integer> valores) {
        double promedio = valores.stream().mapToInt(Integer::intValue).average().orElse(0);
        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    /** null cuando la lista viene vacía: un promedio de cero elementos no es "cero", es "sin datos". */
    private static BigDecimal promedioBigDecimal(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return null;
        }
        double promedio = valores.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0);
        return BigDecimal.valueOf(promedio).setScale(2, RoundingMode.HALF_UP);
    }

    private static double porcentaje(long cantidad, int total) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf(cantidad * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /** Los puntajes sin respuestas (null) se ordenan como el peor caso, al inicio del ranking ascendente. */
    private static BigDecimal ordenable(BigDecimal puntaje) {
        return puntaje != null ? puntaje : BigDecimal.valueOf(-1);
    }

    private record Progreso(int respondidas, int esperadas) {
    }
}
