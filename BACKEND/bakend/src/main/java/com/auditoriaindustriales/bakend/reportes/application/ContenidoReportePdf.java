package com.auditoriaindustriales.bakend.reportes.application;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Datos ya resueltos y agregados (mismos que consumen los endpoints de resultados) que necesita el PDF; los arma ReporteService vía ResultadosService. */
public record ContenidoReportePdf(
        String empresaNombre,
        String nit,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        BigDecimal puntajeGlobal,
        String nivelMadurez,
        String colorSemaforo,
        int preguntasRespondidas,
        int preguntasEsperadas,
        List<LineaSubcategoria> subcategorias,
        List<LineaRanking> ranking,
        List<ConteoSeveridadPdf> hallazgosPorSeveridad,
        List<ConteoEstadoPdf> hallazgosPorEstado,
        List<LineaHallazgo> hallazgos,
        List<LineaCategoria> categorias,
        boolean catalogoCompleto,
        int subcategoriasEvaluadas,
        int subcategoriasTotal,
        /** true si la auditoría todavía está en_progreso al momento de generar — informe PRELIMINAR (regla 13 de PROMPT_DASHBOARD_REPORTES.md), no un cierre definitivo. */
        boolean preliminar,
        /** Fecha de corte del informe: "hoy" cuando es preliminar (fechaFin todavía es null); fechaFin de la auditoría cuando es final. */
        LocalDate fechaCorte,
        /** null = informe integral (toda la auditoría, comportamiento de siempre). "categoria" o "subcategoria" = informe independiente (Etapa 5) — todas las listas de arriba ya vienen filtradas a ese alcance. */
        String alcanceTipo,
        /** Nombre de la categoría o subcategoría cuando alcanceTipo no es null. */
        String alcanceNombre) {

    /**
     * Un eje del radar: puntaje actual vs. meta. puntaje null + evaluada=false es una subcategoría
     * pendiente (plano vacío), no un cero. `detalle` es su desglose interno (por sección de
     * su(s) cuestionario(s)) para la mini-gráfica propia de la subcategoría en el PDF.
     */
    public record LineaSubcategoria(
            String subcategoria, String categoria, BigDecimal puntaje, BigDecimal meta, String colorSemaforo,
            boolean evaluada, List<LineaRanking> detalle) {
    }

    /** Una barra del ranking, ya ordenada ascendente y con su color de semáforo (o gris "sin datos" si aún no evaluada). */
    public record LineaRanking(String etiqueta, String categoria, BigDecimal puntaje, String colorHex, boolean evaluada) {
    }

    /**
     * Agregado por categoría: solo trae puntaje cuando completa=true — regla de generación
     * progresiva (histograma + radar propios solo al completar todas sus subcategorías).
     * subcategoriasEnAlcance distingue "sin empezar" de "en progreso" cuando nada está completo.
     */
    public record LineaCategoria(
            String categoria, boolean completa, int subcategoriasCompletas, int subcategoriasTotal,
            BigDecimal puntaje, String colorSemaforo, int subcategoriasEnAlcance) {
    }

    public record ConteoSeveridadPdf(String severidad, int cantidad, double porcentaje, String colorHex) {
    }

    public record ConteoEstadoPdf(String estado, int cantidad, double porcentaje) {
    }

    public record LineaHallazgo(String descripcion, String severidad, String accionRecomendada, String estado, String area) {
    }
}
