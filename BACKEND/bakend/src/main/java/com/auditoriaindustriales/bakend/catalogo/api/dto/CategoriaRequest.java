package com.auditoriaindustriales.bakend.catalogo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion,
        @Size(max = 50) String icono,
        boolean esPlantilla) {
}
