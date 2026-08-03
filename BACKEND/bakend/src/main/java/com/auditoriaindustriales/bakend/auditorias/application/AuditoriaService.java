package com.auditoriaindustriales.bakend.auditorias.application;

import com.auditoriaindustriales.bakend.auditorias.api.AuditoriaMapper;
import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaRepository;
import com.auditoriaindustriales.bakend.empresas.domain.EmpresaRepository;
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
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaRepository auditoriaRepository;
    private final EmpresaRepository empresaRepository;
    private final AuditoriaMapper auditoriaMapper;

    public AuditoriaService(AuditoriaRepository auditoriaRepository, EmpresaRepository empresaRepository, AuditoriaMapper auditoriaMapper) {
        this.auditoriaRepository = auditoriaRepository;
        this.empresaRepository = empresaRepository;
        this.auditoriaMapper = auditoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaResponse> listar() {
        return auditoriaRepository.listar().stream().map(auditoriaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AuditoriaResponse obtener(UUID id) {
        return auditoriaMapper.toResponse(buscarOFallar(id));
    }

    public AuditoriaResponse crear(UUID empresaId, AutenticacionUsuario usuario) {
        empresaRepository.buscarPorId(empresaId).orElseThrow(() -> NotFoundException.of("Empresa", empresaId));
        Auditoria auditoria = Auditoria.iniciar(empresaId, usuario.usuarioId());
        Auditoria guardada = auditoriaRepository.guardar(auditoria);
        log.info("auditoria={} iniciada por usuario={} para empresa={}", guardada.getId(), usuario.usuarioId(), empresaId);
        return auditoriaMapper.toResponse(guardada);
    }

    public AuditoriaResponse finalizar(UUID id, AutenticacionUsuario usuario) {
        Auditoria auditoria = buscarOFallar(id);
        auditoria.exigirPermisoEscritura(usuario);
        auditoria.finalizar();
        log.info("auditoria={} finalizada por usuario={}", id, usuario.usuarioId());
        return auditoriaMapper.toResponse(auditoriaRepository.guardar(auditoria));
    }

    public AuditoriaResponse cancelar(UUID id, AutenticacionUsuario usuario) {
        Auditoria auditoria = buscarOFallar(id);
        auditoria.exigirPermisoEscritura(usuario);
        auditoria.cancelar();
        log.info("auditoria={} cancelada por usuario={}", id, usuario.usuarioId());
        return auditoriaMapper.toResponse(auditoriaRepository.guardar(auditoria));
    }

    /** Punto de entrada público para que otros módulos (ej. reportes) validen permisos sin exponer la entidad. */
    public void exigirPermisoEscritura(UUID auditoriaId, AutenticacionUsuario usuario) {
        buscarOFallar(auditoriaId).exigirPermisoEscritura(usuario);
    }

    Auditoria buscarOFallar(UUID id) {
        return auditoriaRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Auditoría", id));
    }
}
