package com.auditoriaindustriales.bakend.reportes.infrastructure;

import com.auditoriaindustriales.bakend.reportes.application.ContenidoReportePdf;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ejercita GraficoReporteFactory/PdfReporteGenerator (JFreeChart + PDFBox)
 * de punta a punta sin Spring ni Postgres: no depende de Docker, así que
 * corre siempre, a diferencia de los tests de integración de Testcontainers.
 */
class PdfReporteGeneratorTest {

    private final PdfReporteGenerator generador = new PdfReporteGenerator();

    @Test
    void generaUnPdfConGraficasEmbebidas() {
        ContenidoReportePdf contenido = new ContenidoReportePdf(
                "Empresa de Prueba", "900123456-1",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                new BigDecimal("3.00"), "Preventivo", "#D4860A",
                3, 3,
                List.of(
                        new ContenidoReportePdf.LineaSubcategoria("Diagnóstico energético", "Energía", new BigDecimal("4.00"), new BigDecimal("4.0"), "#27AE60", true,
                                List.of(new ContenidoReportePdf.LineaRanking("Auditoría energética — diagnóstico", null, new BigDecimal("4.00"), "#27AE60", true))),
                        new ContenidoReportePdf.LineaSubcategoria("Cultura de mantenimiento", "Mantenimiento", new BigDecimal("2.00"), new BigDecimal("4.0"), "#C0392B", true,
                                List.of(new ContenidoReportePdf.LineaRanking("Checklist de cultura de mantenimiento", null, new BigDecimal("2.00"), "#C0392B", true))),
                        new ContenidoReportePdf.LineaSubcategoria("Órdenes de trabajo", "Mantenimiento", null, new BigDecimal("4.0"), "#8896A8", false, List.of())),
                List.of(
                        new ContenidoReportePdf.LineaRanking("Cultura de mantenimiento", "Mantenimiento", new BigDecimal("2.00"), "#C0392B", true),
                        new ContenidoReportePdf.LineaRanking("Órdenes de trabajo", "Mantenimiento", null, "#8896A8", false),
                        new ContenidoReportePdf.LineaRanking("Diagnóstico energético", "Energía", new BigDecimal("4.00"), "#27AE60", true)),
                List.of(
                        new ContenidoReportePdf.ConteoSeveridadPdf("critica", 1, 33.3, "#C0392B"),
                        new ContenidoReportePdf.ConteoSeveridadPdf("media", 1, 33.3, "#D4860A"),
                        new ContenidoReportePdf.ConteoSeveridadPdf("baja", 1, 33.3, "#27AE60")),
                List.of(
                        new ContenidoReportePdf.ConteoEstadoPdf("abierto", 2, 66.7),
                        new ContenidoReportePdf.ConteoEstadoPdf("cerrado", 1, 33.3)),
                List.of(new ContenidoReportePdf.LineaHallazgo("Hallazgo crítico", "critica", "Actuar ya", "abierto", "Mantenimiento")),
                List.of(
                        new ContenidoReportePdf.LineaCategoria("Energía", true, 1, 1, new BigDecimal("4.00"), "#27AE60", 1),
                        new ContenidoReportePdf.LineaCategoria("Mantenimiento", false, 1, 2, null, "#8896A8", 1)),
                false, 2, 3,
                false, LocalDate.of(2026, 1, 15), null, null);

        byte[] pdf = generador.generar(contenido);

        assertThat(pdf).isNotEmpty();
        // El PDF con radar + ranking + dona embebidos es órdenes de magnitud más grande que el texto plano original.
        assertThat(pdf.length).isGreaterThan(20_000);
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void generaElRadarGeneralPorCategoriaSoloCuandoElCatalogoEstaCompleto() {
        ContenidoReportePdf contenido = new ContenidoReportePdf(
                "Empresa de Prueba", "900123456-1",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15),
                new BigDecimal("3.75"), "Predictivo", "#27AE60",
                2, 2,
                List.of(
                        new ContenidoReportePdf.LineaSubcategoria("Diagnóstico energético", "Energía", new BigDecimal("4.00"), new BigDecimal("4.0"), "#27AE60", true, List.of()),
                        new ContenidoReportePdf.LineaSubcategoria("Cultura de mantenimiento", "Mantenimiento", new BigDecimal("3.50"), new BigDecimal("4.0"), "#D4860A", true, List.of())),
                List.of(),
                List.of(), List.of(), List.of(),
                List.of(
                        new ContenidoReportePdf.LineaCategoria("Energía", true, 1, 1, new BigDecimal("4.00"), "#27AE60", 1),
                        new ContenidoReportePdf.LineaCategoria("Mantenimiento", true, 1, 1, new BigDecimal("3.50"), "#D4860A", 1)),
                true, 2, 2,
                false, LocalDate.of(2026, 1, 15), null, null);

        byte[] pdf = generador.generar(contenido);

        assertThat(pdf).isNotEmpty();
        // El radar general (por categoría, no por subcategoría) solo aparece cuando catalogoCompleto=true.
        assertThat(pdf.length).isGreaterThan(15_000);
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }

    @Test
    void noRompeConPreguntasIncompletasEnUnCuestionarioAplicado() throws Exception {
        // Regresión: "⚠" (U+26A0) no existe en WinAnsiEncoding/Helvetica — PDFBox lanzaba
        // IllegalArgumentException al dibujar el aviso de "auditoría incompleta". Ningún test
        // anterior ejercitaba preguntasRespondidas < preguntasEsperadas, así que el crash pasó
        // desapercibido hasta que una auditoría real con un cuestionario a medio responder
        // intentó generar su informe preliminar (Etapa 1) y reventó con 500.
        ContenidoReportePdf contenido = new ContenidoReportePdf(
                "Empresa de Prueba", "900123456-1",
                LocalDate.of(2026, 1, 1), null,
                new BigDecimal("2.81"), "Reactivo", "#D4860A",
                3, 22,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                false, 1, 7,
                true, LocalDate.of(2026, 1, 10), null, null);

        byte[] pdf = generador.generar(contenido);

        String texto = extraerTexto(pdf);
        assertThat(texto).contains("[!] 3 de 22 preguntas respondidas");
    }

    @Test
    void marcaClaramentePreliminarVsFinalConFechaDeCorteYAvance() throws Exception {
        ContenidoReportePdf base = new ContenidoReportePdf(
                "Empresa de Prueba", "900123456-1",
                LocalDate.of(2026, 1, 1), null,
                new BigDecimal("3.00"), "Preventivo", "#D4860A",
                3, 3,
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                false, 2, 5,
                true, LocalDate.of(2026, 1, 10), null, null);

        String textoPreliminar = extraerTexto(generador.generar(base));
        assertThat(textoPreliminar).contains("INFORME PRELIMINAR");
        assertThat(textoPreliminar).contains("Fecha de corte: 10/01/2026");
        assertThat(textoPreliminar).contains("Avance del catálogo: 2 de 5 subcategorías evaluadas (40%)");
        assertThat(textoPreliminar).doesNotContain("INFORME FINAL");

        ContenidoReportePdf finalizado = new ContenidoReportePdf(
                base.empresaNombre(), base.nit(), base.fechaInicio(), LocalDate.of(2026, 1, 20),
                base.puntajeGlobal(), base.nivelMadurez(), base.colorSemaforo(),
                base.preguntasRespondidas(), base.preguntasEsperadas(),
                base.subcategorias(), base.ranking(), base.hallazgosPorSeveridad(), base.hallazgosPorEstado(), base.hallazgos(),
                base.categorias(), base.catalogoCompleto(), base.subcategoriasEvaluadas(), base.subcategoriasTotal(),
                false, LocalDate.of(2026, 1, 20), null, null);

        String textoFinal = extraerTexto(generador.generar(finalizado));
        assertThat(textoFinal).contains("INFORME FINAL");
        assertThat(textoFinal).contains("Fecha fin: 20/01/2026");
        assertThat(textoFinal).doesNotContain("INFORME PRELIMINAR");
        assertThat(textoFinal).doesNotContain("Avance del catálogo");
    }

    @Test
    void informeDeCategoriaNoMuestraElRadarGeneralNiElLenguajeDeCatalogo() throws Exception {
        ContenidoReportePdf contenido = new ContenidoReportePdf(
                "Empresa de Prueba", "900123456-1",
                LocalDate.of(2026, 1, 1), null,
                new BigDecimal("3.50"), "Preventivo", "#D4860A",
                0, 0,
                List.of(
                        new ContenidoReportePdf.LineaSubcategoria("Diagnóstico", "Energía", new BigDecimal("4.00"), new BigDecimal("4.0"), "#27AE60", true,
                                List.of(new ContenidoReportePdf.LineaRanking("General", null, new BigDecimal("4.00"), "#27AE60", true))),
                        new ContenidoReportePdf.LineaSubcategoria("Sostenibilidad", "Energía", new BigDecimal("3.00"), new BigDecimal("4.0"), "#E4A317", true,
                                List.of(new ContenidoReportePdf.LineaRanking("General", null, new BigDecimal("3.00"), "#E4A317", true)))),
                List.of(
                        new ContenidoReportePdf.LineaRanking("Sostenibilidad", "Energía", new BigDecimal("3.00"), "#E4A317", true),
                        new ContenidoReportePdf.LineaRanking("Diagnóstico", "Energía", new BigDecimal("4.00"), "#27AE60", true)),
                List.of(), List.of(), List.of(),
                List.of(new ContenidoReportePdf.LineaCategoria("Energía", true, 2, 2, new BigDecimal("3.50"), "#D4860A", 2)),
                true, 2, 2,
                false, LocalDate.of(2026, 1, 20), "categoria", "Energía");

        // Nota: los títulos de las gráficas (JFreeChart) quedan dentro del PNG incrustado, no como
        // texto extraíble por PDFBox — por eso esta prueba solo verifica el texto plano real.
        String texto = extraerTexto(generador.generar(contenido));
        assertThat(texto).contains("Informe de Categoría: Energía");
        assertThat(texto).contains("Fortalezas");
        assertThat(texto).contains("Aspectos prioritarios");
        assertThat(texto).doesNotContain("Cobertura del catálogo");
        assertThat(texto).doesNotContain("Categorías pendientes de completar");
    }

    @Test
    void informeDeSubcategoriaMuestraSoloEsaSubcategoriaConSuDesglose() throws Exception {
        ContenidoReportePdf contenido = new ContenidoReportePdf(
                "Empresa de Prueba", "900123456-1",
                LocalDate.of(2026, 1, 1), null,
                new BigDecimal("2.81"), "Reactivo", "#D4860A",
                0, 0,
                List.of(new ContenidoReportePdf.LineaSubcategoria("Cultura de mantenimiento", "Mantenimiento", new BigDecimal("2.81"), new BigDecimal("4.0"), "#D4860A", true,
                        List.of(
                                new ContenidoReportePdf.LineaRanking("Trabajo en equipo", null, new BigDecimal("2.00"), "#E0805C", true),
                                new ContenidoReportePdf.LineaRanking("Compromiso gerencial", null, new BigDecimal("4.00"), "#2E9E5B", true)))),
                List.of(new ContenidoReportePdf.LineaRanking("Cultura de mantenimiento", "Mantenimiento", new BigDecimal("2.81"), "#D4860A", true)),
                List.of(), List.of(), List.of(),
                List.of(),
                false, 1, 1,
                true, LocalDate.of(2026, 1, 20), "subcategoria", "Cultura de mantenimiento");

        String texto = extraerTexto(generador.generar(contenido));
        assertThat(texto).contains("Informe de Subcategoría: Cultura de mantenimiento");
        assertThat(texto).contains("Subcategoría: Cultura de mantenimiento");
        assertThat(texto).contains("Fortalezas");
        assertThat(texto).doesNotContain("Categoría:");
    }

    private static String extraerTexto(byte[] pdf) throws Exception {
        try (PDDocument documento = Loader.loadPDF(pdf)) {
            return new PDFTextStripper().getText(documento);
        }
    }

    @Test
    void generaUnPdfSinGraficasCuandoNoHayDatos() {
        ContenidoReportePdf contenido = new ContenidoReportePdf(
                "Empresa Vacía", "900000000-0",
                LocalDate.of(2026, 1, 1), null,
                null, null, "#C0392B",
                0, 0,
                List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), false, 0, 0,
                true, LocalDate.of(2026, 1, 1), null, null);

        byte[] pdf = generador.generar(contenido);

        assertThat(pdf).isNotEmpty();
        assertThat(new String(pdf, 0, 5, java.nio.charset.StandardCharsets.US_ASCII)).isEqualTo("%PDF-");
    }
}
