package com.auditoriaindustriales.bakend.reportes.application;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaResponse;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaCuestionarioService;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaService;
import com.auditoriaindustriales.bakend.auditorias.application.HallazgoService;
import com.auditoriaindustriales.bakend.auditorias.domain.EstadoAuditoria;
import com.auditoriaindustriales.bakend.catalogo.application.CuestionarioService;
import com.auditoriaindustriales.bakend.empresas.application.EmpresaService;
import com.auditoriaindustriales.bakend.reportes.api.ReporteMapper;
import com.auditoriaindustriales.bakend.reportes.api.dto.ReporteResponse;
import com.auditoriaindustriales.bakend.reportes.domain.Reporte;
import com.auditoriaindustriales.bakend.reportes.domain.ReporteRepository;
import com.auditoriaindustriales.bakend.reportes.infrastructure.PdfReporteGenerator;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import com.auditoriaindustriales.bakend.shared.infrastructure.SupabaseStorageClient;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ReporteService {

    private static final Logger log = LoggerFactory.getLogger(ReporteService.class);

    private final ReporteRepository reporteRepository;
    private final AuditoriaService auditoriaService;
    private final AuditoriaCuestionarioService auditoriaCuestionarioService;
    private final HallazgoService hallazgoService;
    private final CuestionarioService cuestionarioService;
    private final EmpresaService empresaService;
    private final PdfReporteGenerator pdfGenerator;
    private final SupabaseStorageClient storageClient;
    private final ReporteMapper reporteMapper;
    private final String bucket;

    public ReporteService(
            ReporteRepository reporteRepository,
            AuditoriaService auditoriaService,
            AuditoriaCuestionarioService auditoriaCuestionarioService,
            HallazgoService hallazgoService,
            CuestionarioService cuestionarioService,
            EmpresaService empresaService,
            PdfReporteGenerator pdfGenerator,
            SupabaseStorageClient storageClient,
            ReporteMapper reporteMapper,
            @Value("${app.supabase.storage.bucket-reportes}") String bucket) {
        this.reporteRepository = reporteRepository;
        this.auditoriaService = auditoriaService;
        this.auditoriaCuestionarioService = auditoriaCuestionarioService;
        this.hallazgoService = hallazgoService;
        this.cuestionarioService = cuestionarioService;
        this.empresaService = empresaService;
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

    /** Solo se generan reportes de auditorías ya finalizadas, con puntaje global ya calculado por el trigger. */
    public ReporteResponse generar(UUID auditoriaId, AutenticacionUsuario usuario) {
        auditoriaService.exigirPermisoEscritura(auditoriaId, usuario);
        AuditoriaResponse auditoria = auditoriaService.obtener(auditoriaId);

        if (auditoria.estado() != EstadoAuditoria.FINALIZADA) {
            throw new ConflictException("Solo se pueden generar reportes de auditorías finalizadas (estado actual: %s).".formatted(auditoria.estado()));
        }
        if (auditoria.puntajeGlobal() == null) {
            throw new ConflictException("La auditoría no tiene puntaje calculado: no se registraron respuestas.");
        }

        ContenidoReportePdf contenido = construirContenido(auditoriaId, auditoria);
        byte[] pdf = pdfGenerator.generar(contenido);

        String path = "%s/reporte.pdf".formatted(auditoriaId);
        String url = storageClient.subir(bucket, path, pdf, "application/pdf");

        Reporte reporte = Reporte.generar(auditoriaId, auditoria.puntajeGlobal(), url);
        Reporte guardado = reporteRepository.guardar(reporte);
        log.info("reporte={} generado por usuario={} para auditoria={} (nivelMadurez={})",
                guardado.getId(), usuario.usuarioId(), auditoriaId, guardado.getNivelMadurez());
        return reporteMapper.toResponse(guardado);
    }

    private ContenidoReportePdf construirContenido(UUID auditoriaId, AuditoriaResponse auditoria) {
        String empresaNombre = empresaService.obtener(auditoria.empresaId()).razonSocial();

        List<ContenidoReportePdf.LineaCuestionario> cuestionarios = auditoriaCuestionarioService.listarPorAuditoria(auditoriaId).stream()
                .map(ac -> new ContenidoReportePdf.LineaCuestionario(
                        cuestionarioService.obtener(ac.cuestionarioId()).nombre(), ac.puntaje(), ac.estado().name()))
                .toList();

        List<ContenidoReportePdf.LineaHallazgo> hallazgos = hallazgoService.listarPorAuditoria(auditoriaId).stream()
                .map(h -> new ContenidoReportePdf.LineaHallazgo(h.descripcion(), h.severidad().name(), h.estado().name(), h.area()))
                .toList();

        String nivelMadurez = com.auditoriaindustriales.bakend.reportes.domain.NivelMadurez.desde(auditoria.puntajeGlobal()).getEtiqueta();

        return new ContenidoReportePdf(
                empresaNombre, auditoria.fechaInicio(), auditoria.fechaFin(), auditoria.puntajeGlobal(), nivelMadurez, cuestionarios, hallazgos);
    }
}
