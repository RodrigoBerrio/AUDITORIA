package com.auditoriaindustriales.bakend.catalogo.api.dto;

import java.util.UUID;

public record SubcategoriaResponse(
        UUID id, UUID categoriaId, String nombre, String descripcion, String responsable, boolean esPlantilla, boolean activo) {
}
