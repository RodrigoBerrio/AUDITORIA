package com.auditoriaindustriales.bakend.shared.domain;

import java.math.BigDecimal;

/**
 * Escala de 4 niveles para el desglose por criterio/dimensión dentro de una subcategoría (cortes
 * en 2/3/4, distinta del semáforo único de 3 niveles — {@link Semaforo}, cortes en 2.5/3.75 — que
 * sigue gobernando subcategoría/categoría/auditoría completa: KPI, radar, ranking, dona y PDF a
 * ese nivel no cambian). Antes esta escala solo vivía en el frontend (config/semaforoDetalle.ts,
 * recalculada ahí mismo); el PDF nunca la reproducía y el criterio de color quedaba duplicado
 * entre dos lugares. Ahora el backend es la única fuente — mismos hex a ambos lados.
 */
public enum SemaforoDetalle {
    CRITICO("#C0392B"),
    SERIO("#E0805C"),
    ALERTA("#E4A317"),
    BUENO("#2E9E5B"),
    /** Sin puntaje todavía — gris neutro, nunca rojo (misma regla que Semaforo.SIN_DATOS). */
    SIN_DATOS("#8896A8");

    private final String colorHex;

    SemaforoDetalle(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getColorHex() {
        return colorHex;
    }

    public static SemaforoDetalle desde(BigDecimal puntaje) {
        if (puntaje == null) {
            return SIN_DATOS;
        }
        double valor = puntaje.doubleValue();
        if (valor < 2) {
            return CRITICO;
        }
        if (valor < 3) {
            return SERIO;
        }
        if (valor < 4) {
            return ALERTA;
        }
        return BUENO;
    }
}
