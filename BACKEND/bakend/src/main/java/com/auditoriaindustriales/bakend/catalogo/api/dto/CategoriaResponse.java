package com.auditoriaindustriales.bakend.catalogo.api.dto;

import java.util.UUID;

public record CategoriaResponse(UUID id, String nombre, String descripcion, String icono, boolean esPlantilla, boolean activo) {
}
