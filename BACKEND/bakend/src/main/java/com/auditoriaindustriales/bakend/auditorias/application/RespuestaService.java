package com.auditoriaindustriales.bakend.auditorias.application;

import com.auditoriaindustriales.bakend.auditorias.api.RespuestaMapper;
import com.auditoriaindustriales.bakend.auditorias.api.dto.RespuestaRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.RespuestaResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaCuestionario;
import com.auditoriaindustriales.bakend.auditorias.domain.Respuesta;
import com.auditoriaindustriales.bakend.auditorias.domain.RespuestaRepository;
import com.auditoriaindustriales.bakend.catalogo.domain.PreguntaRepository;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RespuestaService {

    private static final Logger log = LoggerFactory.getLogger(RespuestaService.class);

    private final RespuestaRepository respuestaRepository;
    private final PreguntaRepository preguntaRepository;
    private final AuditoriaCuestionarioService auditoriaCuestionarioService;
    private final AuditoriaService auditoriaService;
    private final RespuestaMapper respuestaMapper;

    public RespuestaService(
            RespuestaRepository respuestaRepository,
            PreguntaRepository preguntaRepository,
            AuditoriaCuestionarioService auditoriaCuestionarioService,
            AuditoriaService auditoriaService,
            RespuestaMapper respuestaMapper) {
        this.respuestaRepository = respuestaRepository;
        this.preguntaRepository = preguntaRepository;
        this.auditoriaCuestionarioService = auditoriaCuestionarioService;
        this.auditoriaService = auditoriaService;
        this.respuestaMapper = respuestaMapper;
    }

    @Transactional(readOnly = true)
    public List<RespuestaResponse> listarPorAuditoriaCuestionario(UUID auditoriaCuestionarioId) {
        return respuestaRepository.listarPorAuditoriaCuestionario(auditoriaCuestionarioId).stream()
                .map(respuestaMapper::toResponse).toList();
    }

    /** Upsert: si ya existe una respuesta para esa pregunta en ese cuestionario aplicado, la edita en vez de duplicarla. */
    public RespuestaResponse responder(UUID auditoriaCuestionarioId, RespuestaRequest request, AutenticacionUsuario usuario) {
        AuditoriaCuestionario auditoriaCuestionario = auditoriaCuestionarioService.buscarOFallar(auditoriaCuestionarioId);
        Auditoria auditoria = auditoriaService.buscarOFallar(auditoriaCuestionario.getAuditoriaId());
        auditoria.exigirPermisoEscritura(usuario);

        preguntaRepository.buscarPorId(request.preguntaId()).orElseThrow(() -> NotFoundException.of("Pregunta", request.preguntaId()));

        Respuesta respuesta = respuestaRepository.buscarPorAuditoriaCuestionarioYPregunta(auditoriaCuestionarioId, request.preguntaId())
                .map(existente -> {
                    existente.actualizar(request.valor(), request.observacion());
                    return existente;
                })
                .orElseGet(() -> Respuesta.registrar(auditoriaCuestionarioId, request.preguntaId(), request.valor(), request.observacion()));

        Respuesta guardada = respuestaRepository.guardar(respuesta);
        // El trigger fn_sync_puntaje_cuestionario ya recalculó el puntaje; solo falta
        // reflejar en el dominio que el cuestionario aplicado dejó de estar "pendiente".
        auditoriaCuestionarioService.marcarEnProgresoSiCorresponde(auditoriaCuestionario);

        log.info("respuesta a pregunta={} valor={} registrada por usuario={} en auditoria={}",
                request.preguntaId(), request.valor(), usuario.usuarioId(), auditoria.getId());
        return respuestaMapper.toResponse(guardada);
    }
}
