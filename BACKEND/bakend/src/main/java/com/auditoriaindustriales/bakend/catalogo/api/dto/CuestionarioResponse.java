package com.auditoriaindustriales.bakend.catalogo.api.dto;

import java.util.UUID;

public record CuestionarioResponse(UUID id, UUID subcategoriaId, String nombre, int numPreguntas, boolean activo) {
}
