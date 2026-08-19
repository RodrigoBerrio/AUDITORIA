package com.auditoriaindustriales.bakend.catalogo.api.dto;

import java.util.UUID;

public record PreguntaResponse(UUID id, UUID cuestionarioId, int numero, String texto, String evidencia, String seccion, boolean activo) {
}
