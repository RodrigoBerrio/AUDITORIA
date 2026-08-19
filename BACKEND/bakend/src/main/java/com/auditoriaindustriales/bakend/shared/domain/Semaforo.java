package com.auditoriaindustriales.bakend.shared.domain;

import java.math.BigDecimal;

/**
 * Umbrales de semáforo únicos de la app (regla no negociable del spec de
 * resultados gráficos): rojo/crítico si puntaje &lt; 2.5, ámbar/en desarrollo
 * si 2.5-3.74, verde/consolidado si puntaje &gt;= 3.75. Los mismos hex ya se
 * usan en theme.css/ReportesPage.tsx del frontend, así que KPI, radar,
 * ranking, dona y el PDF lucen exactamente igual sin coordinar manualmente.
 * Distinto de NivelMadurez (5 niveles, otra escala, solo para la etiqueta
 * del reporte): nadie debe recalcular estos umbrales por su cuenta.
 */
public enum Semaforo {
    CRITICO("#C0392B"),
    EN_DESARROLLO("#D4860A"),
    CONSOLIDADO("#27AE60"),
    /** Sin puntaje todavía (subcategoría/categoría pendiente de evaluar) — gris neutro, nunca rojo: no es "crítico", es "sin medir". Mismo tono que --text-3 del frontend. */
    SIN_DATOS("#8896A8");

    private final String colorHex;

    Semaforo(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getColorHex() {
        return colorHex;
    }

    /** OJO: puntaje null aquí significa "sin datos", no "crítico" — quien necesite distinguir pendiente de mal puntaje debe chequear null antes de llamar. */
    public static Semaforo desde(BigDecimal puntaje) {
        if (puntaje == null) {
            return SIN_DATOS;
        }
        double valor = puntaje.doubleValue();
        if (valor < 2.5) {
            return CRITICO;
        }
        if (valor < 3.75) {
            return EN_DESARROLLO;
        }
        return CONSOLIDADO;
    }
}
