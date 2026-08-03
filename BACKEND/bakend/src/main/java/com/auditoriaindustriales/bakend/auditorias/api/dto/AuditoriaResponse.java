package com.auditoriaindustriales.bakend.auditorias.api.dto;

import com.auditoriaindustriales.bakend.auditorias.domain.EstadoAuditoria;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AuditoriaResponse(
        UUID id, UUID empresaId, UUID auditorId, EstadoAuditoria estado,
        BigDecimal puntajeGlobal, LocalDate fechaInicio, LocalDate fechaFin) {
}
