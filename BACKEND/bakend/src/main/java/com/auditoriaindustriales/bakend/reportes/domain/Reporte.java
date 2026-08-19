package com.auditoriaindustriales.bakend.reportes.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * rutaPdf nunca queda vacío apuntando a "pendiente": el reporte solo se crea
 * una vez que el PDF ya fue generado y subido a Supabase Storage (ver
 * ReporteService.generar), así que el constructor exige la URL.
 */
@Entity
@Table(name = "reporte")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auditoria_id", nullable = false)
    private UUID auditoriaId;

    @Column(name = "puntaje_total")
    private BigDecimal puntajeTotal;

    @Column(name = "nivel_madurez", length = 50)
    private String nivelMadurez;

    @Column(name = "ruta_pdf", length = 255)
    private String rutaPdf;

    /** null = alcance integral (toda la auditoría) — mismo significado que antes de que existiera esta columna. */
    @Column(name = "alcance_tipo", length = 20)
    private String alcanceTipo;

    /** Nombre de la categoría o subcategoría cuando alcanceTipo no es null; null junto con alcanceTipo si es integral. */
    @Column(name = "alcance_nombre", length = 150)
    private String alcanceNombre;

    @Column(name = "generado_en", insertable = false, updatable = false)
    private Instant generadoEn;

    protected Reporte() {
        // JPA
    }

    private Reporte(UUID auditoriaId, BigDecimal puntajeTotal, String rutaPdf, String alcanceTipo, String alcanceNombre) {
        this.auditoriaId = auditoriaId;
        this.puntajeTotal = puntajeTotal;
        // puntajeTotal es null cuando el informe es de una categoría/subcategoría (Etapa 5) que
        // todavía está en progreso — legítimo, no exigimos completarla para generar su preliminar.
        this.nivelMadurez = puntajeTotal != null ? NivelMadurez.desde(puntajeTotal).getEtiqueta() : null;
        this.rutaPdf = Objects.requireNonNull(rutaPdf, "rutaPdf no puede ser nulo: el PDF ya debe estar subido antes de crear el reporte");
        this.alcanceTipo = alcanceTipo;
        this.alcanceNombre = alcanceNombre;
    }

    /** Alcance integral (toda la auditoría) — el caso de siempre. */
    public static Reporte generar(UUID auditoriaId, BigDecimal puntajeTotal, String rutaPdf) {
        return new Reporte(auditoriaId, puntajeTotal, rutaPdf, null, null);
    }

    /** Alcance acotado a una categoría o subcategoría (Etapa 5 — informes independientes). */
    public static Reporte generarConAlcance(UUID auditoriaId, BigDecimal puntajeTotal, String rutaPdf, String alcanceTipo, String alcanceNombre) {
        return new Reporte(auditoriaId, puntajeTotal, rutaPdf,
                Objects.requireNonNull(alcanceTipo, "alcanceTipo no puede ser nulo"),
                Objects.requireNonNull(alcanceNombre, "alcanceNombre no puede ser nulo"));
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuditoriaId() {
        return auditoriaId;
    }

    public BigDecimal getPuntajeTotal() {
        return puntajeTotal;
    }

    public String getNivelMadurez() {
        return nivelMadurez;
    }

    public String getRutaPdf() {
        return rutaPdf;
    }

    public String getAlcanceTipo() {
        return alcanceTipo;
    }

    public String getAlcanceNombre() {
        return alcanceNombre;
    }

    public Instant getGeneradoEn() {
        return generadoEn;
    }
}
