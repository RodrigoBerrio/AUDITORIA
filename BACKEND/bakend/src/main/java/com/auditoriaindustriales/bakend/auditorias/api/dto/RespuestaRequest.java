package com.auditoriaindustriales.bakend.auditorias.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RespuestaRequest(
        @NotNull UUID preguntaId,
        @Min(1) @Max(5) int valor,
        @Size(max = 500) String observacion) {
}
