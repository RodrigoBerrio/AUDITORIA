package com.auditoriaindustriales.bakend.reportes.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ReporteTest {

    @Test
    void generarConPuntajeCalculaElNivelDeMadurez() {
        Reporte reporte = Reporte.generar(UUID.randomUUID(), new BigDecimal("3.50"), "http://storage/reporte.pdf");

        assertThat(reporte.getNivelMadurez()).isEqualTo("Preventivo");
    }

    @Test
    void generarConAlcanceYPuntajeNuloNoRevienta() {
        // Regresión: un informe de categoría/subcategoría (Etapa 5) todavía en progreso puede
        // legítimamente no tener puntaje — NivelMadurez.desde(null) lanzaba NullPointerException
        // porque solo estaba pensado para el puntaje_global de la auditoría, siempre no-nulo.
        assertThatCode(() -> Reporte.generarConAlcance(
                UUID.randomUUID(), null, "http://storage/reporte-categoria.pdf", "categoria", "Mantenimiento"))
                .doesNotThrowAnyException();

        Reporte reporte = Reporte.generarConAlcance(
                UUID.randomUUID(), null, "http://storage/reporte-categoria.pdf", "categoria", "Mantenimiento");

        assertThat(reporte.getPuntajeTotal()).isNull();
        assertThat(reporte.getNivelMadurez()).isNull();
        assertThat(reporte.getAlcanceTipo()).isEqualTo("categoria");
        assertThat(reporte.getAlcanceNombre()).isEqualTo("Mantenimiento");
    }
}
