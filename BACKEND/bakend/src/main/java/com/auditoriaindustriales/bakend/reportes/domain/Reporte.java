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

    @Column(name = "generado_en", insertable = false, updatable = false)
    private Instant generadoEn;

    protected Reporte() {
        // JPA
    }

    private Reporte(UUID auditoriaId, BigDecimal puntajeTotal, String rutaPdf) {
        this.auditoriaId = auditoriaId;
        this.puntajeTotal = puntajeTotal;
        this.nivelMadurez = NivelMadurez.desde(puntajeTotal).getEtiqueta();
        this.rutaPdf = Objects.requireNonNull(rutaPdf, "rutaPdf no puede ser nulo: el PDF ya debe estar subido antes de crear el reporte");
    }

    public static Reporte generar(UUID auditoriaId, BigDecimal puntajeTotal, String rutaPdf) {
        return new Reporte(auditoriaId, puntajeTotal, rutaPdf);
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

    public Instant getGeneradoEn() {
        return generadoEn;
    }
}
