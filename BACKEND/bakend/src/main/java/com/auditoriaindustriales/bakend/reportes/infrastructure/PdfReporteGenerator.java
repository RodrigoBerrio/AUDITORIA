package com.auditoriaindustriales.bakend.reportes.infrastructure;

import com.auditoriaindustriales.bakend.reportes.application.ContenidoReportePdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * Genera el PDF del reporte con el mismo orden que la vista web de
 * resultados: portada, KPI + nivel de madurez, resumen ejecutivo, radar de
 * subcategorías vs. meta (catálogo completo — placeholders para lo
 * pendiente), ranking de barras, semáforo de subcategorías evaluadas,
 * secciones por categoría (histograma + radar propios solo si la categoría
 * está 100% evaluada), dona de hallazgos por severidad + avance por estado,
 * y tabla final de hallazgos. Las gráficas son imágenes PNG reales
 * (GraficoReporteFactory + JFreeChart), no capturas de pantalla ni links
 * externos.
 */
@Component
public class PdfReporteGenerator {

    private static final float MARGEN = 50f;
    private static final float ALTO_LINEA = 16f;
    private static final PDFont FUENTE = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FUENTE_NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generar(ContenidoReportePdf contenido) {
        try (PDDocument documento = new PDDocument()) {
            EscritorPaginado escritor = new EscritorPaginado(documento);

            boolean esIntegral = contenido.alcanceTipo() == null;

            escribirPortadaYKpi(escritor, contenido);
            escribirResumenEjecutivo(escritor, contenido);
            if (!esIntegral) {
                escribirFortalezasYPrioritarios(escritor, contenido);
            }

            // El radar general NUNCA mezcla subcategorías de categorías distintas en un solo
            // gráfico (regla corregida del spec): a nivel catálogo usa categorías como eje, y solo
            // se genera cuando las 3 categorías están 100% evaluadas Y el informe es integral —
            // un informe de categoría/subcategoría (Etapa 5) no compara contra otras categorías.
            if (esIntegral && contenido.catalogoCompleto() && !contenido.categorias().isEmpty()) {
                escritor.escribirImagen(GraficoReporteFactory.radarCategorias(contenido.categorias()));
                escritor.escribirImagen(GraficoReporteFactory.rankingBarras("Histograma general por categoría", histogramaCategorias(contenido.categorias())));
            }
            if (!contenido.ranking().isEmpty()) {
                String tituloRanking = esIntegral ? "Ranking de subcategorías (catálogo completo)" : "Ranking de subcategorías — " + contenido.alcanceNombre();
                escritor.escribirImagen(GraficoReporteFactory.rankingBarras(tituloRanking, contenido.ranking()));
            }
            if (!"subcategoria".equals(contenido.alcanceTipo()) && contenido.subcategorias().stream().anyMatch(ContenidoReportePdf.LineaSubcategoria::evaluada)) {
                escritor.escribirImagen(GraficoReporteFactory.donaSemaforoSubcategorias(contenido.subcategorias()));
            }

            if ("subcategoria".equals(contenido.alcanceTipo())) {
                escribirEnfoqueSubcategoria(escritor, contenido);
            } else {
                escribirSeccionesPorCategoria(escritor, contenido);
            }

            if (contenido.hallazgosPorSeveridad().stream().anyMatch(c -> c.cantidad() > 0)) {
                escritor.escribirImagen(GraficoReporteFactory.donaHallazgos(contenido.hallazgosPorSeveridad()));
            }

            escribirAvanceHallazgos(escritor, contenido);
            escribirTablaHallazgos(escritor, contenido);

            escritor.cerrar();

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el PDF del reporte.", ex);
        }
    }

    /** Convierte el agregado por categoría en barras ordenadas ascendente — mismo componente visual que el histograma de subcategorías, un nivel más arriba. */
    private static List<ContenidoReportePdf.LineaRanking> histogramaCategorias(List<ContenidoReportePdf.LineaCategoria> categorias) {
        return categorias.stream()
                .sorted(Comparator.comparing(c -> c.puntaje() != null ? c.puntaje() : java.math.BigDecimal.valueOf(-1)))
                .map(c -> new ContenidoReportePdf.LineaRanking(c.categoria(), null, c.puntaje(), c.colorSemaforo(), c.completa()))
                .toList();
    }

    /** Inspirado en el formato de reportes ejecutivos de referencia: hallazgos clave numerados antes del detalle gráfico. */
    private void escribirResumenEjecutivo(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        if (contenido.subcategoriasTotal() == 0) {
            return;
        }
        escritor.escribir(FUENTE_NEGRITA, 13, "Resumen ejecutivo");

        if (contenido.alcanceTipo() == null) {
            escritor.escribir(FUENTE, 11, "  - Cobertura del catálogo: %d de %d subcategorías evaluadas."
                    .formatted(contenido.subcategoriasEvaluadas(), contenido.subcategoriasTotal()));
            if (contenido.catalogoCompleto()) {
                escritor.escribir(FUENTE, 11, "  - Auditoría integral completa: calificación general y radar generados con el 100% del catálogo.");
            } else {
                List<String> pendientes = contenido.categorias().stream()
                        .filter(c -> !c.completa())
                        .map(ContenidoReportePdf.LineaCategoria::categoria)
                        .toList();
                if (!pendientes.isEmpty()) {
                    escritor.escribir(FUENTE, 11, "  - Categorías pendientes de completar: " + String.join(", ", pendientes) + ".");
                }
            }
        }

        List<ContenidoReportePdf.LineaSubcategoria> evaluadas = contenido.subcategorias().stream()
                .filter(ContenidoReportePdf.LineaSubcategoria::evaluada)
                .toList();
        if (!evaluadas.isEmpty()) {
            ContenidoReportePdf.LineaSubcategoria masDebil = evaluadas.stream()
                    .min(Comparator.comparing(ContenidoReportePdf.LineaSubcategoria::puntaje)).orElseThrow();
            ContenidoReportePdf.LineaSubcategoria masFuerte = evaluadas.stream()
                    .max(Comparator.comparing(ContenidoReportePdf.LineaSubcategoria::puntaje)).orElseThrow();
            escritor.escribir(FUENTE, 11, "  - Área más débil: %s (%.1f / 5.0).".formatted(masDebil.subcategoria(), masDebil.puntaje().doubleValue()));
            escritor.escribir(FUENTE, 11, "  - Área más fuerte: %s (%.1f / 5.0).".formatted(masFuerte.subcategoria(), masFuerte.puntaje().doubleValue()));
        }

        long criticosOAltos = contenido.hallazgosPorSeveridad().stream()
                .filter(c -> c.severidad().equalsIgnoreCase("critica") || c.severidad().equalsIgnoreCase("alta"))
                .mapToLong(ContenidoReportePdf.ConteoSeveridadPdf::cantidad)
                .sum();
        if (criticosOAltos > 0) {
            escritor.escribir(FUENTE, 11, "  - %d hallazgo(s) de severidad alta o crítica requieren atención prioritaria.".formatted(criticosOAltos));
        }
        escritor.saltarLinea();
    }

    /**
     * Solo en informes de alcance acotado (categoría/subcategoría — Etapa 5): top 5 mejor y peor
     * puntuados dentro de ese alcance (secciones 19/20 de PROMPT_DASHBOARD_REPORTES.md piden
     * "Fortalezas" y "Aspectos prioritarios", máximo 3-5, en los informes de categoría/subcategoría).
     * Usa `ranking` para alcance de categoría (subcategorías) o el `detalle` de la única
     * subcategoría en alcance de subcategoría (criterios) — nunca inventa datos que no existan.
     */
    private void escribirFortalezasYPrioritarios(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        List<ContenidoReportePdf.LineaRanking> items = "subcategoria".equals(contenido.alcanceTipo())
                ? contenido.subcategorias().stream().findFirst().map(ContenidoReportePdf.LineaSubcategoria::detalle).orElse(List.of())
                : contenido.ranking();

        List<ContenidoReportePdf.LineaRanking> evaluados = items.stream().filter(i -> i.puntaje() != null).toList();
        if (evaluados.isEmpty()) {
            return;
        }

        List<ContenidoReportePdf.LineaRanking> fortalezas = evaluados.stream()
                .sorted(Comparator.comparing(ContenidoReportePdf.LineaRanking::puntaje, Comparator.reverseOrder()))
                .limit(5)
                .toList();
        List<ContenidoReportePdf.LineaRanking> prioritarios = evaluados.stream()
                .sorted(Comparator.comparing(ContenidoReportePdf.LineaRanking::puntaje))
                .limit(5)
                .toList();

        escritor.escribir(FUENTE_NEGRITA, 13, "Fortalezas");
        for (var f : fortalezas) {
            escritor.escribir(FUENTE, 11, "  - %s (%.1f / 5.0)".formatted(f.etiqueta(), f.puntaje().doubleValue()));
        }
        escritor.saltarLinea();

        escritor.escribir(FUENTE_NEGRITA, 13, "Aspectos prioritarios");
        for (var p : prioritarios) {
            escritor.escribir(FUENTE, 11, "  - %s (%.1f / 5.0)".formatted(p.etiqueta(), p.puntaje().doubleValue()));
        }
        escritor.saltarLinea();
    }

    /** Informe de UNA subcategoría (Etapa 5): puntaje + desglose por criterio, sin radar (un solo eje no aporta) ni comparación contra otras subcategorías. */
    private void escribirEnfoqueSubcategoria(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        ContenidoReportePdf.LineaSubcategoria sub = contenido.subcategorias().stream().findFirst().orElse(null);
        if (sub == null) {
            return;
        }
        escritor.escribir(FUENTE_NEGRITA, 14, "Subcategoría: " + sub.subcategoria());
        if (!sub.evaluada()) {
            escritor.escribir(FUENTE, 11, "En progreso — todavía no se completan todos sus cuestionarios.");
            escritor.saltarLinea();
            return;
        }
        escritor.escribir(FUENTE, 11, "Completa — %.1f / 5.0".formatted(sub.puntaje().doubleValue()));
        if (!sub.detalle().isEmpty()) {
            escritor.escribirImagen(GraficoReporteFactory.rankingBarras("Desglose por criterio — " + sub.subcategoria(), sub.detalle()));
        }
        escritor.saltarLinea();
    }

    /**
     * Regla de generación progresiva: histograma y radar propios de una categoría solo cuando sus
     * subcategorías están 100% evaluadas. El radar de la categoría se arma SOLO con sus propias
     * subcategorías (nunca mezclado con las de otra categoría — error corregido del spec), y cada
     * subcategoría completa recibe además su propia mini-gráfica de desglose interno.
     */
    private void escribirSeccionesPorCategoria(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        if (contenido.categorias().isEmpty()) {
            return;
        }
        for (ContenidoReportePdf.LineaCategoria categoria : contenido.categorias()) {
            escritor.escribir(FUENTE_NEGRITA, 14, "Categoría: " + categoria.categoria());
            if (!categoria.completa()) {
                String estado = categoria.subcategoriasEnAlcance() == 0 ? "No iniciada" : "En progreso";
                escritor.escribir(FUENTE, 11, "%s — %d de %d subcategorías evaluadas (%d en alcance)."
                        .formatted(estado, categoria.subcategoriasCompletas(), categoria.subcategoriasTotal(), categoria.subcategoriasEnAlcance()));
                escritor.saltarLinea();
                continue;
            }
            escritor.escribir(FUENTE, 11, "Completa — %.1f / 5.0".formatted(categoria.puntaje().doubleValue()));

            List<ContenidoReportePdf.LineaSubcategoria> subs = contenido.subcategorias().stream()
                    .filter(s -> s.categoria().equals(categoria.categoria()))
                    .toList();
            List<ContenidoReportePdf.LineaRanking> histogramaCategoria = contenido.ranking().stream()
                    .filter(r -> categoria.categoria().equals(r.categoria()))
                    .toList();
            // Radar + histograma propios de la categoría: armados solo con sus subcategorías,
            // nunca mezclados con los de otra categoría (regla corregida del spec).
            if (!subs.isEmpty()) {
                escritor.escribirImagen(GraficoReporteFactory.radarMadurez(subs));
            }
            if (!histogramaCategoria.isEmpty()) {
                escritor.escribirImagen(GraficoReporteFactory.rankingBarras("Histograma — " + categoria.categoria(), histogramaCategoria));
            }
            for (ContenidoReportePdf.LineaSubcategoria sub : subs) {
                if (!sub.detalle().isEmpty()) {
                    escritor.escribirImagen(GraficoReporteFactory.rankingBarras(sub.subcategoria(), sub.detalle()));
                }
            }
            escritor.saltarLinea();
        }
    }

    /**
     * PRELIMINAR (auditoría todavía en_progreso) vs. FINAL (finalizada) — regla 13 del spec: el
     * informe preliminar debe declarar claramente qué fue evaluado, qué está pendiente, la fecha
     * de corte y el % de avance, nunca presentarse como si fuera el cierre definitivo.
     */
    private void escribirPortadaYKpi(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        String titulo = switch (String.valueOf(contenido.alcanceTipo())) {
            case "categoria" -> "Informe de Categoría: %s — %s".formatted(contenido.alcanceNombre(), contenido.empresaNombre());
            case "subcategoria" -> "Informe de Subcategoría: %s — %s".formatted(contenido.alcanceNombre(), contenido.empresaNombre());
            default -> "Reporte de Auditoría — " + contenido.empresaNombre();
        };
        escritor.escribir(FUENTE_NEGRITA, 16, titulo);
        escritor.escribir(FUENTE_NEGRITA, 13, contenido.preliminar() ? "INFORME PRELIMINAR" : "INFORME FINAL");
        escritor.escribir(FUENTE, 11, "NIT: " + valorOGuion(contenido.nit()));
        escritor.escribir(FUENTE, 11, "Fecha inicio: " + formatear(contenido.fechaInicio()));
        if (contenido.preliminar()) {
            escritor.escribir(FUENTE, 11, "Fecha de corte: " + formatear(contenido.fechaCorte()));
            int porcentaje = contenido.subcategoriasTotal() > 0
                    ? Math.round(contenido.subcategoriasEvaluadas() * 100f / contenido.subcategoriasTotal())
                    : 0;
            String etiquetaAvance = switch (String.valueOf(contenido.alcanceTipo())) {
                case "categoria" -> "Avance de la categoría";
                case "subcategoria" -> "Avance de la subcategoría";
                default -> "Avance del catálogo";
            };
            escritor.escribir(FUENTE, 11, "%s: %d de %d subcategorías evaluadas (%d%%)."
                    .formatted(etiquetaAvance, contenido.subcategoriasEvaluadas(), contenido.subcategoriasTotal(), porcentaje));
            escritor.escribir(FUENTE, 10, "Los resultados reflejan únicamente lo evaluado hasta la fecha de corte; lo pendiente NO se contabiliza como cero.");
        } else {
            escritor.escribir(FUENTE, 11, "Fecha fin: " + formatear(contenido.fechaFin()));
        }
        escritor.saltarLinea();
        escritor.escribir(FUENTE_NEGRITA, 13, "Puntaje global: " + valorOGuion(contenido.puntajeGlobal()) + " / 5.0");
        escritor.escribir(FUENTE, 11, "Nivel de madurez: " + valorOGuion(contenido.nivelMadurez()));
        if (contenido.preguntasEsperadas() > 0 && contenido.preguntasRespondidas() < contenido.preguntasEsperadas()) {
            // "⚠" (U+26A0) no existe en WinAnsiEncoding/Helvetica — PDFBox lanza IllegalArgumentException
            // al intentar dibujarlo. "[!]" es el equivalente ASCII-safe.
            escritor.escribir(FUENTE, 10, "[!] %d de %d preguntas respondidas en los cuestionarios aplicados — el promedio puede no ser representativo."
                    .formatted(contenido.preguntasRespondidas(), contenido.preguntasEsperadas()));
        }
        escritor.saltarLinea();
    }

    private void escribirAvanceHallazgos(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        escritor.escribir(FUENTE_NEGRITA, 13, "Avance del plan de acción (hallazgos por estado)");
        List<ContenidoReportePdf.ConteoEstadoPdf> porEstado = contenido.hallazgosPorEstado();
        if (porEstado.isEmpty() || porEstado.stream().allMatch(c -> c.cantidad() == 0)) {
            escritor.escribir(FUENTE, 11, "  (sin hallazgos registrados)");
        } else {
            for (var c : porEstado) {
                escritor.escribir(FUENTE, 11, "  - %s: %d (%.1f%%)".formatted(c.estado(), c.cantidad(), c.porcentaje()));
            }
        }
        escritor.saltarLinea();
    }

    private void escribirTablaHallazgos(EscritorPaginado escritor, ContenidoReportePdf contenido) throws IOException {
        escritor.escribir(FUENTE_NEGRITA, 13, "Detalle de hallazgos");
        if (contenido.hallazgos().isEmpty()) {
            escritor.escribir(FUENTE, 11, "  (sin hallazgos registrados)");
            return;
        }
        for (var h : contenido.hallazgos()) {
            escritor.escribir(FUENTE, 11, "  - [%s/%s] %s (área: %s)".formatted(h.severidad(), h.estado(), h.descripcion(), valorOGuion(h.area())));
            if (h.accionRecomendada() != null && !h.accionRecomendada().isBlank()) {
                escritor.escribir(FUENTE, 10, "      Acción recomendada: " + h.accionRecomendada());
            }
        }
    }

    private String formatear(java.time.LocalDate fecha) {
        return fecha == null ? "—" : fecha.format(FORMATO_FECHA);
    }

    private String valorOGuion(Object valor) {
        return valor == null ? "—" : valor.toString();
    }

    /** Envuelve PDPageContentStream y crea página nueva automáticamente al llegar al margen inferior (texto e imágenes). */
    private static final class EscritorPaginado {
        private final PDDocument documento;
        private PDPageContentStream stream;
        private float y;

        EscritorPaginado(PDDocument documento) throws IOException {
            this.documento = documento;
            nuevaPagina();
        }

        void escribir(PDFont fuente, float tamano, String texto) throws IOException {
            if (y < MARGEN + ALTO_LINEA) {
                nuevaPagina();
            }
            stream.beginText();
            stream.setFont(fuente, tamano);
            stream.newLineAtOffset(MARGEN, y);
            stream.showText(texto);
            stream.endText();
            y -= ALTO_LINEA;
        }

        void escribirImagen(byte[] png) throws IOException {
            PDImageXObject imagen = PDImageXObject.createFromByteArray(documento, png, "grafica.png");
            float anchoDisponible = PDRectangle.LETTER.getWidth() - 2 * MARGEN;
            float escala = Math.min(1f, anchoDisponible / imagen.getWidth());
            float ancho = imagen.getWidth() * escala;
            float alto = imagen.getHeight() * escala;

            if (y < MARGEN + alto) {
                nuevaPagina();
            }
            stream.drawImage(imagen, MARGEN, y - alto, ancho, alto);
            y -= alto + ALTO_LINEA / 2;
        }

        void saltarLinea() {
            y -= ALTO_LINEA / 2;
        }

        void nuevaPagina() throws IOException {
            if (stream != null) {
                stream.close();
            }
            PDPage pagina = new PDPage(PDRectangle.LETTER);
            documento.addPage(pagina);
            stream = new PDPageContentStream(documento, pagina);
            y = PDRectangle.LETTER.getHeight() - MARGEN;
        }

        void cerrar() throws IOException {
            stream.close();
        }
    }
}
