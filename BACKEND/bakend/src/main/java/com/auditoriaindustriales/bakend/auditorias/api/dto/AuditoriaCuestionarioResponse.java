package com.auditoriaindustriales.bakend.auditorias.api.dto;

import com.auditoriaindustriales.bakend.auditorias.domain.EstadoCuestionario;

import java.math.BigDecimal;
import java.util.UUID;

public record AuditoriaCuestionarioResponse(
        UUID id, UUID auditoriaId, UUID cuestionarioId, EstadoCuestionario estado, BigDecimal puntaje) {
}
