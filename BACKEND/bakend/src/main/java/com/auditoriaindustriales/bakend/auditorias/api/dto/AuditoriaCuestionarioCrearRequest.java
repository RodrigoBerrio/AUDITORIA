package com.auditoriaindustriales.bakend.auditorias.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuditoriaCuestionarioCrearRequest(@NotNull UUID cuestionarioId) {
}
