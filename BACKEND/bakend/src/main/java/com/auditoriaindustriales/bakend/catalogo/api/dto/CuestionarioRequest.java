package com.auditoriaindustriales.bakend.catalogo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CuestionarioRequest(@NotBlank @Size(max = 150) String nombre) {
}
