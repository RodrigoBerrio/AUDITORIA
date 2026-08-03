package com.auditoriaindustriales.bakend.auditorias.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "respuesta")
public class Respuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auditoria_cuestionario_id", nullable = false)
    private UUID auditoriaCuestionarioId;

    @Column(name = "pregunta_id", nullable = false)
    private UUID preguntaId;

    @Column(nullable = false)
    private int valor;

    @Column(length = 500)
    private String observacion;

    // No hay trigger de BD que lo actualice en un UPDATE (el DEFAULT now()
    // solo aplica al INSERT); por eso el dominio lo reescribe explícitamente
    // en cada edición para que siga reflejando "cuándo se respondió".
    @Column(name = "respondido_en", insertable = false)
    private Instant respondidoEn;

    protected Respuesta() {
        // JPA
    }

    private Respuesta(UUID auditoriaCuestionarioId, UUID preguntaId, int valor, String observacion) {
        this.auditoriaCuestionarioId = auditoriaCuestionarioId;
        this.preguntaId = preguntaId;
        this.valor = valor;
        this.observacion = observacion;
    }

    public static Respuesta registrar(UUID auditoriaCuestionarioId, UUID preguntaId, int valor, String observacion) {
        return new Respuesta(auditoriaCuestionarioId, preguntaId, valor, observacion);
    }

    public void actualizar(int valor, String observacion) {
        this.valor = valor;
        this.observacion = observacion;
        this.respondidoEn = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuditoriaCuestionarioId() {
        return auditoriaCuestionarioId;
    }

    public UUID getPreguntaId() {
        return preguntaId;
    }

    public int getValor() {
        return valor;
    }

    public String getObservacion() {
        return observacion;
    }

    public Instant getRespondidoEn() {
        return respondidoEn;
    }
}
