package com.auditoriaindustriales.bakend.auditorias.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

/** clienteUuid se genera en el frontend antes de subir la foto (ver EvidenciaService.subir). */
public record EvidenciaSubidaRequest(
        @NotNull UUID clienteUuid,
        UUID respuestaId,
        @Size(max = 255) String descripcion,
        Instant tomadaEn) {
}
