package com.auditoriaindustriales.bakend.shared.web;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Forma única de error JSON para toda la API; nunca se expone un stack trace. */
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<CampoInvalido> erroresCampos) {

    public record CampoInvalido(String campo, String mensaje) {}

    public static ApiErrorResponse of(int status, String error, String message, String path) {
        return new ApiErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ApiErrorResponse ofValidacion(String path, List<CampoInvalido> errores) {
        return new ApiErrorResponse(Instant.now(), 400, "Bad Request", "Error de validación", path, errores);
    }
}
