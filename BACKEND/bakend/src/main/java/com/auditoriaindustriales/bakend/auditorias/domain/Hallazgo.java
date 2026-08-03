package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "hallazgo")
public class Hallazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auditoria_id", nullable = false)
    private UUID auditoriaId;

    @Column(nullable = false, length = 500)
    private String descripcion;

    @Column(nullable = false, length = 20)
    private Severidad severidad;

    @Column(name = "accion_recomendada", length = 500)
    private String accionRecomendada;

    @Column(nullable = false, length = 20)
    private EstadoHallazgo estado;

    // v4: el auditor la registra manualmente, no se deriva de categoría/subcategoría.
    @Column(length = 100)
    private String area;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Hallazgo() {
        // JPA
    }

    private Hallazgo(UUID auditoriaId, String descripcion, Severidad severidad, String accionRecomendada, String area) {
        this.auditoriaId = auditoriaId;
        this.descripcion = descripcion;
        this.severidad = severidad;
        this.accionRecomendada = accionRecomendada;
        this.area = area;
        this.estado = EstadoHallazgo.ABIERTO;
    }

    public static Hallazgo crear(UUID auditoriaId, String descripcion, Severidad severidad, String accionRecomendada, String area) {
        return new Hallazgo(auditoriaId, descripcion, severidad, accionRecomendada, area);
    }

    public void actualizar(String descripcion, Severidad severidad, String accionRecomendada, String area) {
        this.descripcion = descripcion;
        this.severidad = severidad;
        this.accionRecomendada = accionRecomendada;
        this.area = area;
    }

    public void cambiarEstado(EstadoHallazgo nuevo) {
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

    public String getDescripcion() {
        return descripcion;
    }

    public Severidad getSeveridad() {
        return severidad;
    }

    public String getAccionRecomendada() {
        return accionRecomendada;
    }

    public EstadoHallazgo getEstado() {
        return estado;
    }

    public String getArea() {
        return area;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
