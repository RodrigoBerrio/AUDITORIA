package com.auditoriaindustriales.bakend.auditorias.application;

import com.auditoriaindustriales.bakend.auditorias.api.AuditoriaCuestionarioMapper;
import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaCuestionarioResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaCuestionario;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaCuestionarioRepository;
import com.auditoriaindustriales.bakend.catalogo.domain.CuestionarioRepository;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuditoriaCuestionarioService {

    private final AuditoriaCuestionarioRepository auditoriaCuestionarioRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final AuditoriaService auditoriaService;
    private final AuditoriaCuestionarioMapper mapper;

    public AuditoriaCuestionarioService(
            AuditoriaCuestionarioRepository auditoriaCuestionarioRepository,
            CuestionarioRepository cuestionarioRepository,
            AuditoriaService auditoriaService,
            AuditoriaCuestionarioMapper mapper) {
        this.auditoriaCuestionarioRepository = auditoriaCuestionarioRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.auditoriaService = auditoriaService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaCuestionarioResponse> listarPorAuditoria(UUID auditoriaId) {
        return auditoriaCuestionarioRepository.listarPorAuditoria(auditoriaId).stream().map(mapper::toResponse).toList();
    }

    public AuditoriaCuestionarioResponse aplicarCuestionario(UUID auditoriaId, UUID cuestionarioId, AutenticacionUsuario usuario) {
        Auditoria auditoria = auditoriaService.buscarOFallar(auditoriaId);
        auditoria.exigirPermisoEscritura(usuario);
        cuestionarioRepository.buscarPorId(cuestionarioId).orElseThrow(() -> NotFoundException.of("Cuestionario", cuestionarioId));

        if (auditoriaCuestionarioRepository.existePorAuditoriaYCuestionario(auditoriaId, cuestionarioId)) {
            throw new ConflictException("Este cuestionario ya fue aplicado a esta auditoría.");
        }
        AuditoriaCuestionario auditoriaCuestionario = AuditoriaCuestionario.aplicar(auditoriaId, cuestionarioId);
        return mapper.toResponse(auditoriaCuestionarioRepository.guardar(auditoriaCuestionario));
    }

    public AuditoriaCuestionarioResponse completar(UUID id, AutenticacionUsuario usuario) {
        AuditoriaCuestionario auditoriaCuestionario = buscarOFallar(id);
        Auditoria auditoria = auditoriaService.buscarOFallar(auditoriaCuestionario.getAuditoriaId());
        auditoria.exigirPermisoEscritura(usuario);

        auditoriaCuestionario.completar();
        return mapper.toResponse(auditoriaCuestionarioRepository.guardar(auditoriaCuestionario));
    }

    AuditoriaCuestionario buscarOFallar(UUID id) {
        return auditoriaCuestionarioRepository.buscarPorId(id)
                .orElseThrow(() -> NotFoundException.of("Cuestionario aplicado a la auditoría", id));
    }

    /** Usado por RespuestaService al registrar la primera respuesta de un cuestionario aplicado. */
    void marcarEnProgresoSiCorresponde(AuditoriaCuestionario auditoriaCuestionario) {
        auditoriaCuestionario.marcarEnProgresoSiCorresponde();
        auditoriaCuestionarioRepository.guardar(auditoriaCuestionario);
    }
}
