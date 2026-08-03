package com.auditoriaindustriales.bakend.catalogo.api.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Solo campos sin historial protegido (nunca texto); siempre permitido. */
public record PreguntaActualizarRequest(@Positive int numero, @Size(max = 255) String evidencia) {
}
