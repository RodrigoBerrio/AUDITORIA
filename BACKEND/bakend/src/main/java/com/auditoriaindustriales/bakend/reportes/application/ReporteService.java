package com.auditoriaindustriales.bakend.reportes.application;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaResponse;
import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoResponse;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaService;
import com.auditoriaindustriales.bakend.auditorias.application.HallazgoService;
import com.auditoriaindustriales.bakend.auditorias.domain.EstadoAuditoria;
import com.auditoriaindustriales.bakend.empresas.api.dto.EmpresaResponse;
import com.auditoriaindustriales.bakend.empresas.application.EmpresaService;
import com.auditoriaindustriales.bakend.reportes.api.ReporteMapper;
import com.auditoriaindustriales.bakend.reportes.api.dto.ReporteResponse;
import com.auditoriaindustriales.bakend.reportes.domain.NivelMadurez;
import com.auditoriaindustriales.bakend.reportes.domain.Reporte;
import com.auditoriaindustriales.bakend.reportes.domain.ReporteRepository;
import com.auditoriaindustriales.bakend.reportes.infrastructure.PdfReporteGenerator;
import com.auditoriaindustriales.bakend.resultados.api.dto.HallazgosResumenResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.RankingResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.ResumenAuditoriaResponse;
import com.auditoriaindustriales.bakend.resultados.application.ResultadosService;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import com.auditoriaindustriales.bakend.shared.infrastructure.SupabaseStorageClient;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    private final ReporteRepository reporteRepository;
    private final AuditoriaService auditoriaService;
    private final HallazgoService hallazgoService;
    private final EmpresaService empresaService;
    private final ResultadosService resultadosService;
    private final PdfReporteGenerator pdfGenerator;
    private final SupabaseStorageClient storageClient;
    private final ReporteMapper reporteMapper;
    private final String bucket;

    public ReporteService(
            ReporteRepository reporteRepository,
            AuditoriaService auditoriaService,
            HallazgoService hallazgoService,
            EmpresaService empresaService,
            ResultadosService resultadosService,
            PdfReporteGenerator pdfGenerator,
            SupabaseStorageClient storageClient,
            ReporteMapper reporteMapper,
            @Value("${app.supabase.storage.bucket-reportes}") String bucket) {
        this.reporteRepository = reporteRepository;
        this.auditoriaService = auditoriaService;
        this.hallazgoService = hallazgoService;
        this.empresaService = empresaService;
        this.resultadosService = resultadosService;
        this.pdfGenerator = pdfGenerator;
        this.storageClient = storageClient;
        this.reporteMapper = reporteMapper;
        this.bucket = bucket;
    }

    @Transactional(readOnly = true)
    public List<ReporteResponse> listarPorAuditoria(UUID auditoriaId) {
        return reporteRepository.listarPorAuditoria(auditoriaId).stream().map(reporteMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtener(UUID id) {
        return reporteMapper.toResponse(
                reporteRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Reporte", id)));
    }

    /**
     * Informe PRELIMINAR (auditoría en_progreso) o FINAL (finalizada) — el único requisito real es
     * tener al menos una respuesta registrada (puntaje_global ya calculado por el trigger). No se
     * exige completar el 100% del catálogo ni finalizar la auditoría: eso es justamente lo que
     * distingue un informe preliminar de uno final (regla 11/13 de PROMPT_DASHBOARD_REPORTES.md).
     */
    public ReporteResponse generar(UUID auditoriaId, AutenticacionUsuario usuario) {
        return generar(auditoriaId, usuario, null, null);
    }

    /** Informe independiente de UNA categoría (Etapa 5) — no es la unión de nada, se recalcula el resumen ejecutivo para ese alcance. */
    public ReporteResponse generarPorCategoria(UUID auditoriaId, AutenticacionUsuario usuario, String categoria) {
        return generar(auditoriaId, usuario, "categoria", categoria);
    }

    /** Informe independiente de UNA subcategoría (Etapa 5). */
    public ReporteResponse generarPorSubcategoria(UUID auditoriaId, AutenticacionUsuario usuario, String subcategoria) {
        return generar(auditoriaId, usuario, "subcategoria", subcategoria);
    }

    private ReporteResponse generar(UUID auditoriaId, AutenticacionUsuario usuario, String alcanceTipo, String alcanceNombre) {
        auditoriaService.exigirPermisoEscritura(auditoriaId, usuario);
        AuditoriaResponse auditoria = auditoriaService.obtener(auditoriaId);

        if (auditoria.estado() == EstadoAuditoria.CANCELADA) {
            throw new ConflictException("No se pueden generar reportes de auditorías canceladas.");
        }
        if (auditoria.puntajeGlobal() == null) {
            throw new ConflictException("La auditoría no tiene puntaje calculado: no se registraron respuestas.");
        }

        boolean preliminar = auditoria.estado() != EstadoAuditoria.FINALIZADA;
        ContenidoReportePdf completo = construirContenido(auditoriaId, auditoria, preliminar);
        ContenidoReportePdf contenido = alcanceTipo == null ? completo : acotarAlcance(completo, alcanceTipo, alcanceNombre);

        byte[] pdf = pdfGenerator.generar(contenido);

        String archivo = alcanceTipo == null ? "reporte" : "reporte-%s-%s".formatted(alcanceTipo, slug(alcanceNombre));
        String path = "%s/%s.pdf".formatted(auditoriaId, archivo);
        String url = storageClient.subir(bucket, path, pdf, "application/pdf");

        Reporte reporte = alcanceTipo == null
                ? Reporte.generar(auditoriaId, contenido.puntajeGlobal(), url)
                : Reporte.generarConAlcance(auditoriaId, contenido.puntajeGlobal(), url, contenido.alcanceTipo(), contenido.alcanceNombre());
        Reporte guardado = reporteRepository.guardar(reporte);
        log.info("reporte={} generado por usuario={} para auditoria={} (alcance={}/{}, nivelMadurez={})",
                guardado.getId(), usuario.usuarioId(), auditoriaId, alcanceTipo, alcanceNombre, guardado.getNivelMadurez());
        return reporteMapper.toResponse(guardado);
    }

    /**
     * Recorta el contenido ya armado (misma fuente que el integral, nunca un cálculo aparte) al
     * alcance de una sola categoría o subcategoría. El puntaje/nivel de madurez de portada pasa a
     * ser el de ESE alcance, no el de toda la auditoría — y los hallazgos se filtran por el campo
     * de texto libre `area` (no hay FK estructurada hallazgo→categoría/subcategoría en el modelo
     * actual, así que es un match best-effort por nombre, no garantizado al 100%).
     */
    private ContenidoReportePdf acotarAlcance(ContenidoReportePdf completo, String tipo, String nombre) {
        if ("categoria".equals(tipo)) {
            ContenidoReportePdf.LineaCategoria categoria = completo.categorias().stream()
                    .filter(c -> c.categoria().equalsIgnoreCase(nombre))
                    .findFirst()
                    .orElseThrow(() -> NotFoundException.of("Categoría", nombre));

            List<ContenidoReportePdf.LineaSubcategoria> subs = completo.subcategorias().stream()
                    .filter(s -> s.categoria().equalsIgnoreCase(categoria.categoria()))
                    .toList();
            List<ContenidoReportePdf.LineaRanking> ranking = completo.ranking().stream()
                    .filter(r -> categoria.categoria().equalsIgnoreCase(r.categoria()))
                    .toList();

            Set<String> nombresParaHallazgos = subs.stream().map(ContenidoReportePdf.LineaSubcategoria::subcategoria)
                    .collect(Collectors.toCollection(java.util.HashSet::new));
            nombresParaHallazgos.add(categoria.categoria());
            List<ContenidoReportePdf.LineaHallazgo> hallazgos = filtrarHallazgosPorArea(completo.hallazgos(), nombresParaHallazgos);

            return new ContenidoReportePdf(
                    completo.empresaNombre(), completo.nit(), completo.fechaInicio(), completo.fechaFin(),
                    categoria.puntaje(), nivelMadurezOGuion(categoria.puntaje()), categoria.colorSemaforo(),
                    0, 0,
                    subs, ranking,
                    recontarSeveridad(hallazgos, completo.hallazgosPorSeveridad()),
                    recontarEstado(hallazgos, completo.hallazgosPorEstado()),
                    hallazgos,
                    List.of(categoria), categoria.completa(), categoria.subcategoriasCompletas(), categoria.subcategoriasTotal(),
                    completo.preliminar(), completo.fechaCorte(), "categoria", categoria.categoria());
        }

        if ("subcategoria".equals(tipo)) {
            ContenidoReportePdf.LineaSubcategoria sub = completo.subcategorias().stream()
                    .filter(s -> s.subcategoria().equalsIgnoreCase(nombre))
                    .findFirst()
                    .orElseThrow(() -> NotFoundException.of("Subcategoría", nombre));

            List<ContenidoReportePdf.LineaRanking> ranking = completo.ranking().stream()
                    .filter(r -> sub.subcategoria().equalsIgnoreCase(r.etiqueta()))
                    .toList();
            List<ContenidoReportePdf.LineaHallazgo> hallazgos = filtrarHallazgosPorArea(completo.hallazgos(), Set.of(sub.subcategoria()));

            return new ContenidoReportePdf(
                    completo.empresaNombre(), completo.nit(), completo.fechaInicio(), completo.fechaFin(),
                    sub.puntaje(), nivelMadurezOGuion(sub.puntaje()), sub.colorSemaforo(),
                    0, 0,
                    List.of(sub), ranking,
                    recontarSeveridad(hallazgos, completo.hallazgosPorSeveridad()),
                    recontarEstado(hallazgos, completo.hallazgosPorEstado()),
                    hallazgos,
                    List.of(), sub.evaluada(), sub.evaluada() ? 1 : 0, 1,
                    completo.preliminar(), completo.fechaCorte(), "subcategoria", sub.subcategoria());
        }

        throw new IllegalArgumentException("Tipo de alcance no soportado: " + tipo);
    }

    private static String nivelMadurezOGuion(BigDecimal puntaje) {
        return puntaje != null ? NivelMadurez.desde(puntaje).getEtiqueta() : null;
    }

    /** area es texto libre (sin FK a categoría/subcategoría en el modelo actual) — match best-effort, no garantizado. */
    private static List<ContenidoReportePdf.LineaHallazgo> filtrarHallazgosPorArea(List<ContenidoReportePdf.LineaHallazgo> hallazgos, Set<String> nombres) {
        return hallazgos.stream()
                .filter(h -> h.area() != null && nombres.stream().anyMatch(n -> h.area().equalsIgnoreCase(n)))
                .toList();
    }

    private static List<ContenidoReportePdf.ConteoSeveridadPdf> recontarSeveridad(
            List<ContenidoReportePdf.LineaHallazgo> hallazgosFiltrados, List<ContenidoReportePdf.ConteoSeveridadPdf> original) {
        Map<String, String> colorPorSeveridad = original.stream()
                .collect(Collectors.toMap(ContenidoReportePdf.ConteoSeveridadPdf::severidad, ContenidoReportePdf.ConteoSeveridadPdf::colorHex, (a, b) -> a));
        Map<String, Long> conteo = hallazgosFiltrados.stream()
                .collect(Collectors.groupingBy(ContenidoReportePdf.LineaHallazgo::severidad, Collectors.counting()));
        int total = hallazgosFiltrados.size();
        return colorPorSeveridad.entrySet().stream()
                .map(e -> new ContenidoReportePdf.ConteoSeveridadPdf(
                        e.getKey(), conteo.getOrDefault(e.getKey(), 0L).intValue(), porcentaje(conteo.getOrDefault(e.getKey(), 0L), total), e.getValue()))
                .toList();
    }

    private static List<ContenidoReportePdf.ConteoEstadoPdf> recontarEstado(
            List<ContenidoReportePdf.LineaHallazgo> hallazgosFiltrados, List<ContenidoReportePdf.ConteoEstadoPdf> original) {
        Map<String, Long> conteo = hallazgosFiltrados.stream()
                .collect(Collectors.groupingBy(ContenidoReportePdf.LineaHallazgo::estado, Collectors.counting()));
        int total = hallazgosFiltrados.size();
        return original.stream()
                .map(ContenidoReportePdf.ConteoEstadoPdf::estado)
                .distinct()
                .map(estado -> new ContenidoReportePdf.ConteoEstadoPdf(estado, conteo.getOrDefault(estado, 0L).intValue(), porcentaje(conteo.getOrDefault(estado, 0L), total)))
                .toList();
    }

    private static double porcentaje(long cantidad, int total) {
        if (total == 0) {
            return 0;
        }
        return BigDecimal.valueOf(cantidad * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    /** Nombre de archivo seguro: minúsculas, sin tildes ni espacios ni caracteres especiales. */
    private static String slug(String texto) {
        String sinTildes = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    /** Mismos datos agregados que consumen los endpoints de resultados (ResultadosService) — una sola fuente de verdad para web y PDF. */
    private ContenidoReportePdf construirContenido(UUID auditoriaId, AuditoriaResponse auditoria, boolean preliminar) {
        EmpresaResponse empresa = empresaService.obtener(auditoria.empresaId());

        ResumenAuditoriaResponse resumen = resultadosService.resumen(auditoriaId, null);
        RankingResponse ranking = resultadosService.ranking(auditoriaId);
        HallazgosResumenResponse hallazgosResumen = resultadosService.hallazgosResumen(auditoriaId);

        List<ContenidoReportePdf.LineaSubcategoria> subcategorias = resumen.subcategorias().stream()
                .map(s -> new ContenidoReportePdf.LineaSubcategoria(
                        s.subcategoria(), s.categoria(), s.puntaje(), s.meta(), s.colorSemaforo(), s.evaluada(),
                        s.detalle().stream()
                                .map(d -> new ContenidoReportePdf.LineaRanking(d.etiqueta(), null, d.puntaje(), d.colorSemaforo(), d.evaluada()))
                                .toList()))
                .toList();

        java.util.Map<String, String> categoriaPorSubcategoria = resumen.subcategorias().stream()
                .collect(java.util.stream.Collectors.toMap(
                        ResumenAuditoriaResponse.PuntajeSubcategoria::subcategoria,
                        ResumenAuditoriaResponse.PuntajeSubcategoria::categoria,
                        (a, b) -> a));

        List<ContenidoReportePdf.LineaRanking> lineasRanking = ranking.items().stream()
                .map(i -> new ContenidoReportePdf.LineaRanking(
                        i.etiqueta(), categoriaPorSubcategoria.get(i.etiqueta()), i.puntaje(), i.colorSemaforo(), i.evaluada()))
                .toList();

        List<ContenidoReportePdf.LineaCategoria> lineasCategoria = resumen.categorias().stream()
                .map(c -> new ContenidoReportePdf.LineaCategoria(
                        c.categoria(), c.completa(), c.subcategoriasCompletas(), c.subcategoriasTotal(), c.puntaje(), c.colorSemaforo(), c.subcategoriasEnAlcance()))
                .toList();

        List<ContenidoReportePdf.ConteoSeveridadPdf> porSeveridad = hallazgosResumen.porSeveridad().stream()
                .map(c -> new ContenidoReportePdf.ConteoSeveridadPdf(c.severidad().name(), c.cantidad(), c.porcentaje(), c.colorHex()))
                .toList();

        List<ContenidoReportePdf.ConteoEstadoPdf> porEstado = hallazgosResumen.porEstado().stream()
                .map(c -> new ContenidoReportePdf.ConteoEstadoPdf(c.estado().name(), c.cantidad(), c.porcentaje()))
                .toList();

        List<HallazgoResponse> hallazgos = hallazgoService.listarPorAuditoria(auditoriaId);
        List<ContenidoReportePdf.LineaHallazgo> lineasHallazgo = hallazgos.stream()
                .map(h -> new ContenidoReportePdf.LineaHallazgo(h.descripcion(), h.severidad().name(), h.accionRecomendada(), h.estado().name(), h.area()))
                .toList();

        return new ContenidoReportePdf(
                empresa.razonSocial(), empresa.nit(), auditoria.fechaInicio(), auditoria.fechaFin(),
                resumen.puntajeGlobal(), resumen.nivelMadurez(), resumen.colorSemaforo(),
                resumen.preguntasRespondidas(), resumen.preguntasEsperadas(),
                subcategorias, lineasRanking, porSeveridad, porEstado, lineasHallazgo,
                lineasCategoria, resumen.catalogoCompleto(), resumen.subcategoriasEvaluadas(), resumen.subcategoriasTotal(),
                preliminar, LocalDate.now(), null, null);
    }
}
