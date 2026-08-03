package com.auditoriaindustriales.bakend.catalogo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubcategoriaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 255) String descripcion,
        @Size(max = 100) String responsable,
        boolean esPlantilla) {
}
