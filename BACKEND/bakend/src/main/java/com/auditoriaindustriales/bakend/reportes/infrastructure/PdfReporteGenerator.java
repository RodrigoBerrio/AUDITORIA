package com.auditoriaindustriales.bakend.reportes.infrastructure;

import com.auditoriaindustriales.bakend.reportes.application.ContenidoReportePdf;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

/** Genera un PDF simple de texto plano con el resumen de la auditoría; sin plantillas gráficas por ahora. */
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

            escritor.escribir(FUENTE_NEGRITA, 16, "Reporte de Auditoría — " + contenido.empresaNombre());
            escritor.saltarLinea();
            escritor.escribir(FUENTE, 11, "Fecha inicio: " + formatear(contenido.fechaInicio()));
            escritor.escribir(FUENTE, 11, "Fecha fin: " + formatear(contenido.fechaFin()));
            escritor.escribir(FUENTE, 11, "Puntaje global: " + valorOGuion(contenido.puntajeGlobal()));
            escritor.escribir(FUENTE, 11, "Nivel de madurez: " + valorOGuion(contenido.nivelMadurez()));
            escritor.saltarLinea();

            escritor.escribir(FUENTE_NEGRITA, 13, "Cuestionarios aplicados");
            if (contenido.cuestionarios().isEmpty()) {
                escritor.escribir(FUENTE, 11, "  (sin cuestionarios aplicados)");
            } else {
                for (var c : contenido.cuestionarios()) {
                    escritor.escribir(FUENTE, 11, "  - %s — puntaje: %s — estado: %s"
                            .formatted(c.nombre(), valorOGuion(c.puntaje()), c.estado()));
                }
            }
            escritor.saltarLinea();

            escritor.escribir(FUENTE_NEGRITA, 13, "Hallazgos");
            if (contenido.hallazgos().isEmpty()) {
                escritor.escribir(FUENTE, 11, "  (sin hallazgos registrados)");
            } else {
                for (var h : contenido.hallazgos()) {
                    escritor.escribir(FUENTE, 11, "  - [%s/%s] %s (área: %s)"
                            .formatted(h.severidad(), h.estado(), h.descripcion(), valorOGuion(h.area())));
                }
            }

            escritor.cerrar();

            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            documento.save(salida);
            return salida.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo generar el PDF del reporte.", ex);
        }
    }

    private String formatear(java.time.LocalDate fecha) {
        return fecha == null ? "—" : fecha.format(FORMATO_FECHA);
    }

    private String valorOGuion(Object valor) {
        return valor == null ? "—" : valor.toString();
    }

    /** Envuelve PDPageContentStream y crea página nueva automáticamente al llegar al margen inferior. */
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
