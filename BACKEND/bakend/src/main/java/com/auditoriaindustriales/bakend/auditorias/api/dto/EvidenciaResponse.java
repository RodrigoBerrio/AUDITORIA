package com.auditoriaindustriales.bakend.auditorias.api.dto;

import java.time.Instant;
import java.util.UUID;

public record EvidenciaResponse(
        UUID id, UUID auditoriaId, UUID respuestaId, String urlArchivo, String descripcion,
        UUID subidaPor, UUID clienteUuid, Instant tomadaEn, Instant subidaEn) {
}
