package com.auditoriaindustriales.bakend.auditorias.api.dto;

import java.util.UUID;

public record RespuestaResponse(UUID id, UUID auditoriaCuestionarioId, UUID preguntaId, int valor, String observacion) {
}
