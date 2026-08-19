package com.auditoriaindustriales.bakend.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * texto está protegido por el trigger trg_proteger_texto_pregunta: un UPDATE
 * que lo cambie falla si la pregunta ya tiene respuestas. editarTexto(...)
 * intenta el UPDATE directo (funciona si aún no tiene respuestas); si falla,
 * GlobalExceptionHandler traduce la excepción de Postgres a 409. El caso de
 * uso correcto para una pregunta ya respondida es PreguntaService.reemplazarConHistorial,
 * que desactiva esta instancia y crea una nueva vía crear(...).
 */
@Entity
@Table(name = "pregunta")
public class Pregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private int numero;

    @Column(nullable = false, length = 500)
    private String texto;

    @Column(length = 255)
    private String evidencia;

    /** Agrupación interna dentro del cuestionario (2-8 por checklist); null se trata como "General" al agregar. */
    @Column(length = 100)
    private String seccion;

    @Column(name = "cuestionario_id", nullable = false)
    private UUID cuestionarioId;

    @Column(nullable = false)
    private boolean activo = true;

    protected Pregunta() {
        // JPA
    }

    private Pregunta(UUID cuestionarioId, int numero, String texto, String evidencia, String seccion) {
        this.cuestionarioId = cuestionarioId;
        this.numero = numero;
        this.texto = texto;
        this.evidencia = evidencia;
        this.seccion = seccion;
        this.activo = true;
    }

    public static Pregunta crear(UUID cuestionarioId, int numero, String texto, String evidencia, String seccion) {
        return new Pregunta(cuestionarioId, numero, texto, evidencia, seccion);
    }

    /** Solo cambia campos sin historial protegido; siempre permitido. */
    public void actualizarSinTexto(int numero, String evidencia, String seccion) {
        this.numero = numero;
        this.evidencia = evidencia;
        this.seccion = seccion;
    }

    /** Intento de UPDATE directo del enunciado; el trigger lo rechaza si ya hay respuestas. */
    public void editarTexto(String nuevoTexto) {
        this.texto = nuevoTexto;
    }

    public void desactivar() {
        this.activo = false;
    }

    public UUID getId() {
        return id;
    }

    public int getNumero() {
        return numero;
    }

    public String getTexto() {
        return texto;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public String getSeccion() {
        return seccion;
    }

    public UUID getCuestionarioId() {
        return cuestionarioId;
    }

    public boolean isActivo() {
        return activo;
    }
}
