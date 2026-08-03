package com.auditoriaindustriales.bakend.catalogo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Usado tanto para el intento de edición directa (que el trigger puede
 * rechazar con 409 si la pregunta ya tiene respuestas) como para el
 * reemplazo con historial (desactivar + crear nueva).
 */
public record PreguntaTextoRequest(@NotBlank @Size(max = 500) String texto, @Size(max = 255) String evidencia) {
}
