package com.auditoriaindustriales.bakend.auditorias.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** auditorId no viene del cliente: es siempre el usuario autenticado que crea la auditoría. */
public record AuditoriaCrearRequest(@NotNull UUID empresaId) {
}
