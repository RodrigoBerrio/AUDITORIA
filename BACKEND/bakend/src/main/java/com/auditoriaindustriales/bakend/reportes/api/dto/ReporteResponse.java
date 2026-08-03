package com.auditoriaindustriales.bakend.reportes.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ReporteResponse(
        UUID id, UUID auditoriaId, BigDecimal puntajeTotal, String nivelMadurez, String rutaPdf, Instant generadoEn) {
}
