package com.auditoriaindustriales.bakend.auditorias.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditoriaCuestionarioRepository {

    Optional<AuditoriaCuestionario> buscarPorId(UUID id);

    List<AuditoriaCuestionario> listarPorAuditoria(UUID auditoriaId);

    boolean existePorAuditoriaYCuestionario(UUID auditoriaId, UUID cuestionarioId);

    AuditoriaCuestionario guardar(AuditoriaCuestionario auditoriaCuestionario);
}
