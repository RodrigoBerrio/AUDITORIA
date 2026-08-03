package com.auditoriaindustriales.bakend.reportes.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Datos ya resueltos (nombres, no solo IDs) que necesita el PDF; lo arma ReporteService consultando los demás módulos. */
public record ContenidoReportePdf(
        String empresaNombre,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal puntajeGlobal,
        String nivelMadurez,
        List<LineaCuestionario> cuestionarios,
        List<LineaHallazgo> hallazgos) {

    public record LineaCuestionario(String nombre, BigDecimal puntaje, String estado) {
    }

    public record LineaHallazgo(String descripcion, String severidad, String estado, String area) {
    }
}
