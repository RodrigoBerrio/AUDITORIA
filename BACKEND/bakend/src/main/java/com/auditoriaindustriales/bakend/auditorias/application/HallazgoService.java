package com.auditoriaindustriales.bakend.auditorias.application;

import com.auditoriaindustriales.bakend.auditorias.api.HallazgoMapper;
import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import com.auditoriaindustriales.bakend.auditorias.domain.EstadoHallazgo;
import com.auditoriaindustriales.bakend.auditorias.domain.Hallazgo;
import com.auditoriaindustriales.bakend.auditorias.domain.HallazgoRepository;
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
public class HallazgoService {

    private static final Logger log = LoggerFactory.getLogger(HallazgoService.class);

    private final HallazgoRepository hallazgoRepository;
    private final AuditoriaService auditoriaService;
    private final HallazgoMapper hallazgoMapper;

    public HallazgoService(HallazgoRepository hallazgoRepository, AuditoriaService auditoriaService, HallazgoMapper hallazgoMapper) {
        this.hallazgoRepository = hallazgoRepository;
        this.auditoriaService = auditoriaService;
        this.hallazgoMapper = hallazgoMapper;
    }

    @Transactional(readOnly = true)
    public List<HallazgoResponse> listarPorAuditoria(UUID auditoriaId) {
        return hallazgoRepository.listarPorAuditoria(auditoriaId).stream().map(hallazgoMapper::toResponse).toList();
    }

    public HallazgoResponse crear(UUID auditoriaId, HallazgoRequest request, AutenticacionUsuario usuario) {
        Auditoria auditoria = auditoriaService.buscarOFallar(auditoriaId);
        auditoria.exigirPermisoEscritura(usuario);

        Hallazgo hallazgo = Hallazgo.crear(auditoriaId, request.descripcion(), request.severidad(), request.accionRecomendada(), request.area());
        Hallazgo guardado = hallazgoRepository.guardar(hallazgo);
        log.info("hallazgo={} (severidad={}) creado por usuario={} en auditoria={}",
                guardado.getId(), request.severidad(), usuario.usuarioId(), auditoriaId);
        return hallazgoMapper.toResponse(guardado);
    }

    public HallazgoResponse actualizar(UUID id, HallazgoRequest request, AutenticacionUsuario usuario) {
        Hallazgo hallazgo = buscarOFallar(id);
        Auditoria auditoria = auditoriaService.buscarOFallar(hallazgo.getAuditoriaId());
        auditoria.exigirPermisoEscritura(usuario);

        hallazgo.actualizar(request.descripcion(), request.severidad(), request.accionRecomendada(), request.area());
        return hallazgoMapper.toResponse(hallazgoRepository.guardar(hallazgo));
    }

    public HallazgoResponse cambiarEstado(UUID id, EstadoHallazgo nuevoEstado, AutenticacionUsuario usuario) {
        Hallazgo hallazgo = buscarOFallar(id);
        Auditoria auditoria = auditoriaService.buscarOFallar(hallazgo.getAuditoriaId());
        auditoria.exigirPermisoEscritura(usuario);

        hallazgo.cambiarEstado(nuevoEstado);
        log.info("hallazgo={} cambia a estado={} por usuario={}", id, nuevoEstado, usuario.usuarioId());
        return hallazgoMapper.toResponse(hallazgoRepository.guardar(hallazgo));
    }

    private Hallazgo buscarOFallar(UUID id) {
        return hallazgoRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Hallazgo", id));
    }
}
