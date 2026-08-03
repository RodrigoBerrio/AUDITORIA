package com.auditoriaindustriales.bakend.reportes.domain;

import java.math.BigDecimal;

/**
 * Los 5 niveles de ESCALA_MADUREZ (domain.ts), con los mismos rangos
 * numéricos exactos que el frontend usa para etiquetar reporte.nivel_madurez:
 * 1.0–1.99 De Falla, 2.0–2.99 Reactivo, 3.0–3.99 Preventivo, 4.0–4.99
 * Predictivo, 5.0 Mejores Prácticas. Declarados como constante única del
 * dominio, no como if/else disperso en el servicio.
 */
public enum NivelMadurez {
    DE_FALLA("De Falla"),
    REACTIVO("Reactivo"),
    PREVENTIVO("Preventivo"),
    PREDICTIVO("Predictivo"),
    MEJORES_PRACTICAS("Mejores Prácticas");

    private final String etiqueta;

    NivelMadurez(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static NivelMadurez desde(BigDecimal puntaje) {
        double valor = puntaje.doubleValue();
        if (valor < 2.0) {
            return DE_FALLA;
        }
        if (valor < 3.0) {
            return REACTIVO;
        }
        if (valor < 4.0) {
            return PREVENTIVO;
        }
        if (valor < 5.0) {
            return PREDICTIVO;
        }
        return MEJORES_PRACTICAS;
    }
}
