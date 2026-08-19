package com.auditoriaindustriales.bakend.resultados.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ResumenAuditoriaResponse(
        BigDecimal puntajeGlobal,
        String nivelMadurez,
        String colorSemaforo,
        List<PuntajeSubcategoria> subcategorias,
        int preguntasRespondidas,
        int preguntasEsperadas,
        List<PuntajeCategoria> categorias,
        boolean catalogoCompleto,
        int subcategoriasEvaluadas,
        int subcategoriasTotal,
        /** Alcance inferido (no declarado): cuántas subcategorías del catálogo tienen al menos un cuestionario aplicado, completo o no. */
        int subcategoriasEnAlcance) {

    /**
     * Un eje del radar: puntaje actual vs. meta configurable (por defecto 4.0). Cubre TODO el
     * catálogo activo, no solo lo aplicado a la auditoría: puntaje null + evaluada=false es una
     * subcategoría todavía pendiente. `detalle` es su desglose interno (por sección del/los
     * cuestionario(s) de la subcategoría) para el grid de mini-gráficas por categoría — vacío
     * mientras evaluada=false. `enAlcance` distingue "no iniciada" (false) de "en progreso, sin
     * completar" (true + evaluada=false) — antes ambas se veían igual.
     */
    public record PuntajeSubcategoria(
            String subcategoria, String categoria, BigDecimal puntaje, BigDecimal meta, String colorSemaforo,
            boolean evaluada, List<ItemPuntaje> detalle, boolean enAlcance) {
    }

    /**
     * Agregado por categoría: solo tiene puntaje cuando completa=true (todas sus subcategorías
     * evaluadas) — regla de generación progresiva del reporte. subcategoriasEnAlcance cuenta las
     * que tienen al menos un cuestionario aplicado (completo o no), para distinguir "categoría sin
     * empezar" de "categoría en progreso" cuando ninguna está completa todavía.
     */
    public record PuntajeCategoria(
            String categoria, BigDecimal puntaje, String colorSemaforo, boolean completa,
            int subcategoriasCompletas, int subcategoriasTotal, int subcategoriasEnAlcance) {
    }
}
