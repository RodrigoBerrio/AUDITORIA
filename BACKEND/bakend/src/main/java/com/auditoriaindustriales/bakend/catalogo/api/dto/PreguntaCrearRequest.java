package com.auditoriaindustriales.bakend.catalogo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PreguntaCrearRequest(
        @Positive int numero,
        @NotBlank @Size(max = 500) String texto,
        @Size(max = 255) String evidencia,
        @Size(max = 100) String seccion) {
}
