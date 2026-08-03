package com.auditoriaindustriales.bakend.auditorias.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RespuestaRepository {

    Optional<Respuesta> buscarPorId(UUID id);

    Optional<Respuesta> buscarPorAuditoriaCuestionarioYPregunta(UUID auditoriaCuestionarioId, UUID preguntaId);

    List<Respuesta> listarPorAuditoriaCuestionario(UUID auditoriaCuestionarioId);

    Respuesta guardar(Respuesta respuesta);
}
