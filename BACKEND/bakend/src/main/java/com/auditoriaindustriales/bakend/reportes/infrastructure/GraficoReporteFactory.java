package com.auditoriaindustriales.bakend.reportes.infrastructure;

import com.auditoriaindustriales.bakend.reportes.application.ContenidoReportePdf;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Construye las mismas gráficas que la vista web (radar, ranking, dona) como
 * imágenes PNG reales para insertar en el PDF con PDFBox — nunca capturas de
 * pantalla. Reutiliza los colores de Semaforo/Severidad (ContenidoReportePdf
 * ya los trae calculados) para que el PDF luzca igual que la pantalla.
 */
public final class GraficoReporteFactory {

    private static final Font FUENTE_ETIQUETA = new Font("SansSerif", Font.PLAIN, 11);

    private GraficoReporteFactory() {
    }

    public static byte[] radarMadurez(List<ContenidoReportePdf.LineaSubcategoria> subcategorias) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ContenidoReportePdf.LineaSubcategoria s : subcategorias) {
            dataset.addValue(valorODouble(s.puntaje()), "Puntaje actual", s.subcategoria());
            dataset.addValue(s.meta().doubleValue(), "Meta", s.subcategoria());
        }

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setMaxValue(5.0);
        plot.setLabelFont(FUENTE_ETIQUETA);
        plot.setSeriesPaint(0, new Color(0x2E, 0x6D, 0xA4));
        plot.setSeriesPaint(1, Color.GRAY);
        plot.setSeriesOutlineStroke(1, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 4f}, 0f));
        plot.setBackgroundPaint(Color.WHITE);

        JFreeChart chart = new JFreeChart("Radar de madurez por subcategoría", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        return aPng(chart, 620, 460);
    }

    /** Radar general a nivel catálogo: un eje por CATEGORÍA (nunca por subcategoría mezclada entre categorías). */
    public static byte[] radarCategorias(List<ContenidoReportePdf.LineaCategoria> categorias) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ContenidoReportePdf.LineaCategoria c : categorias) {
            dataset.addValue(valorODouble(c.puntaje()), "Puntaje actual", c.categoria());
            dataset.addValue(4.0, "Meta", c.categoria());
        }

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        plot.setMaxValue(5.0);
        plot.setLabelFont(FUENTE_ETIQUETA);
        plot.setSeriesPaint(0, new Color(0x2E, 0x6D, 0xA4));
        plot.setSeriesPaint(1, Color.GRAY);
        plot.setSeriesOutlineStroke(1, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 4f}, 0f));
        plot.setBackgroundPaint(Color.WHITE);

        JFreeChart chart = new JFreeChart("Radar general por categoría", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        return aPng(chart, 620, 460);
    }

    public static byte[] rankingBarras(String titulo, List<ContenidoReportePdf.LineaRanking> items) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ContenidoReportePdf.LineaRanking item : items) {
            dataset.addValue(valorODouble(item.puntaje()), "Puntaje", item.etiqueta());
        }

        JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(
                titulo, "", "Puntaje (1-5)", dataset, PlotOrientation.HORIZONTAL, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(0xEE, 0xF1, 0xF5));

        NumberAxis ejeValor = (NumberAxis) plot.getRangeAxis();
        ejeValor.setRange(0, 5);

        CategoryAxis ejeCategoria = plot.getDomainAxis();
        ejeCategoria.setTickLabelFont(FUENTE_ETIQUETA);

        BarRenderer renderer = new ColorPorItemRenderer(items);
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", java.text.NumberFormat.getInstance()));
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE3, TextAnchor.CENTER_LEFT));
        plot.setRenderer(renderer);

        return aPng(chart, 620, Math.max(160, items.size() * 42 + 60));
    }

    public static byte[] donaHallazgos(List<ContenidoReportePdf.ConteoSeveridadPdf> conteos) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (ContenidoReportePdf.ConteoSeveridadPdf c : conteos) {
            if (c.cantidad() > 0) {
                dataset.setValue("%s (%d)".formatted(c.severidad(), c.cantidad()), c.cantidad());
            }
        }

        RingPlot plot = new RingPlot(dataset);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(FUENTE_ETIQUETA);
        plot.setSectionDepth(0.35);
        for (ContenidoReportePdf.ConteoSeveridadPdf c : conteos) {
            String clave = "%s (%d)".formatted(c.severidad(), c.cantidad());
            plot.setSectionPaint(clave, Color.decode(c.colorHex()));
        }

        JFreeChart chart = new JFreeChart("Hallazgos por severidad", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        LegendTitle leyenda = chart.getLegend();
        if (leyenda != null) {
            leyenda.setItemFont(FUENTE_ETIQUETA);
        }
        return aPng(chart, 480, 420);
    }

    /** Dona de distribución de semáforo entre las subcategorías ya evaluadas (sin las pendientes: esas no tienen color de desempeño todavía). */
    public static byte[] donaSemaforoSubcategorias(List<ContenidoReportePdf.LineaSubcategoria> subcategorias) {
        java.util.Map<String, Long> conteoPorColor = subcategorias.stream()
                .filter(ContenidoReportePdf.LineaSubcategoria::evaluada)
                .collect(java.util.stream.Collectors.groupingBy(
                        ContenidoReportePdf.LineaSubcategoria::colorSemaforo, java.util.LinkedHashMap::new, java.util.stream.Collectors.counting()));

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (var entrada : conteoPorColor.entrySet()) {
            dataset.setValue("%s (%d)".formatted(etiquetaSemaforo(entrada.getKey()), entrada.getValue()), entrada.getValue());
        }

        RingPlot plot = new RingPlot(dataset);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setLabelFont(FUENTE_ETIQUETA);
        plot.setSectionDepth(0.35);
        for (var entrada : conteoPorColor.entrySet()) {
            String clave = "%s (%d)".formatted(etiquetaSemaforo(entrada.getKey()), entrada.getValue());
            plot.setSectionPaint(clave, Color.decode(entrada.getKey()));
        }

        JFreeChart chart = new JFreeChart("Semáforo de subcategorías evaluadas", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        chart.setBackgroundPaint(Color.WHITE);
        LegendTitle leyenda = chart.getLegend();
        if (leyenda != null) {
            leyenda.setItemFont(FUENTE_ETIQUETA);
        }
        return aPng(chart, 480, 380);
    }

    private static String etiquetaSemaforo(String colorHex) {
        return switch (colorHex.toUpperCase()) {
            case "#27AE60" -> "Consolidado";
            case "#D4860A" -> "En desarrollo";
            case "#C0392B" -> "Crítico";
            default -> "Sin dato";
        };
    }

    private static double valorODouble(java.math.BigDecimal valor) {
        return valor != null ? valor.doubleValue() : 0d;
    }

    private static byte[] aPng(JFreeChart chart, int ancho, int alto) {
        try {
            BufferedImage imagen = chart.createBufferedImage(ancho, alto);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            ChartUtils.writeBufferedImageAsPNG(salida, imagen);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo renderizar una gráfica del reporte.", ex);
        }
    }

    /** Colorea cada barra con el semáforo ya calculado en backend (no por índice de la barra, regla del spec). */
    private static final class ColorPorItemRenderer extends BarRenderer {
        private final List<ContenidoReportePdf.LineaRanking> items;

        ColorPorItemRenderer(List<ContenidoReportePdf.LineaRanking> items) {
            this.items = items;
            setShadowVisible(false);
            setDrawBarOutline(false);
        }

        @Override
        public java.awt.Paint getItemPaint(int row, int column) {
            return Color.decode(items.get(column).colorHex());
        }
    }
}
