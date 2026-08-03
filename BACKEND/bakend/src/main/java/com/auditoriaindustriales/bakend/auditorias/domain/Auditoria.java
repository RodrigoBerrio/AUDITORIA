package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.ForbiddenException;
import com.auditoriaindustriales.bakend.shared.domain.Rol;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "auditor_id", nullable = false)
    private UUID auditorId;

    @Column(nullable = false, length = 20)
    private EstadoAuditoria estado;

    // Recalculado por fn_sync_puntaje_global; el dominio nunca lo escribe.
    @Column(name = "puntaje_global", insertable = false, updatable = false)
    private BigDecimal puntajeGlobal;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Auditoria() {
        // JPA
    }

    private Auditoria(UUID empresaId, UUID auditorId) {
        this.empresaId = empresaId;
        this.auditorId = auditorId;
        this.estado = EstadoAuditoria.EN_PROGRESO;
        this.fechaInicio = LocalDate.now();
    }

    public static Auditoria iniciar(UUID empresaId, UUID auditorId) {
        return new Auditoria(empresaId, auditorId);
    }

    public void finalizar() {
        exigirEnProgreso();
        this.estado = EstadoAuditoria.FINALIZADA;
        this.fechaFin = LocalDate.now();
    }

    public void cancelar() {
        exigirEnProgreso();
        this.estado = EstadoAuditoria.CANCELADA;
        this.fechaFin = LocalDate.now();
    }

    private void exigirEnProgreso() {
        if (estado != EstadoAuditoria.EN_PROGRESO) {
            throw new ConflictException(
                    "Solo se puede cambiar el estado de una auditoría que está en progreso (estado actual: %s).".formatted(estado));
        }
    }

    public boolean esPropietario(UUID usuarioId) {
        return auditorId.equals(usuarioId);
    }

    /**
     * Solo el auditor asignado a esta auditoría, o un admin/supervisor, puede
     * escribir sobre ella o sobre lo que cuelga de ella (cuestionarios
     * aplicados, respuestas, hallazgos, evidencia, reportes). Vive aquí
     * porque es una invariante del propio agregado, no una regla dispersa
     * de infraestructura — así los módulos auditorias y reportes comparten
     * la misma comprobación sin duplicarla.
     */
    public void exigirPermisoEscritura(AutenticacionUsuario usuario) {
        boolean esSupervisorOAdmin = usuario.rol() == Rol.ADMIN || usuario.rol() == Rol.SUPERVISOR;
        if (!esPropietario(usuario.usuarioId()) && !esSupervisorOAdmin) {
            throw new ForbiddenException("Solo el auditor asignado a esta auditoría, o un admin/supervisor, puede modificarla.");
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public UUID getAuditorId() {
        return auditorId;
    }

    public EstadoAuditoria getEstado() {
        return estado;
    }

    public BigDecimal getPuntajeGlobal() {
        return puntajeGlobal;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
