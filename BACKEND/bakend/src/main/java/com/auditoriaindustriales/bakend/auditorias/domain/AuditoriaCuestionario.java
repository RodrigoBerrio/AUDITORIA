package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_cuestionario")
public class AuditoriaCuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auditoria_id", nullable = false)
    private UUID auditoriaId;

    @Column(name = "cuestionario_id", nullable = false)
    private UUID cuestionarioId;

    @Column(nullable = false, length = 20)
    private EstadoCuestionario estado;

    // Recalculado por fn_sync_puntaje_cuestionario; el dominio nunca lo escribe.
    @Column(insertable = false, updatable = false)
    private BigDecimal puntaje;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected AuditoriaCuestionario() {
        // JPA
    }

    private AuditoriaCuestionario(UUID auditoriaId, UUID cuestionarioId) {
        this.auditoriaId = auditoriaId;
        this.cuestionarioId = cuestionarioId;
        this.estado = EstadoCuestionario.PENDIENTE;
    }

    public static AuditoriaCuestionario aplicar(UUID auditoriaId, UUID cuestionarioId) {
        return new AuditoriaCuestionario(auditoriaId, cuestionarioId);
    }

    /** Se dispara automáticamente al registrar la primera respuesta; nunca retrocede. */
    public void marcarEnProgresoSiCorresponde() {
        if (estado == EstadoCuestionario.PENDIENTE) {
            estado = EstadoCuestionario.EN_PROGRESO;
        }
    }

    public void completar() {
        cambiarEstado(EstadoCuestionario.COMPLETADO);
    }

    private void cambiarEstado(EstadoCuestionario nuevo) {
        if (!estado.esTransicionValidaHacia(nuevo)) {
            throw new ConflictException("Transición de estado inválida: de %s a %s.".formatted(estado, nuevo));
        }
        estado = nuevo;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuditoriaId() {
        return auditoriaId;
    }

    public UUID getCuestionarioId() {
        return cuestionarioId;
    }

    public EstadoCuestionario getEstado() {
        return estado;
    }

    public BigDecimal getPuntaje() {
        return puntaje;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
