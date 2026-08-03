package com.auditoriaindustriales.bakend.catalogo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cuestionario")
public class Cuestionario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombre;

    // Recalculado por el trigger fn_sync_num_preguntas en cada INSERT/UPDATE/DELETE
    // sobre pregunta. El dominio nunca lo escribe.
    @Column(name = "num_preguntas", insertable = false, updatable = false)
    private int numPreguntas;

    @Column(name = "subcategoria_id", nullable = false)
    private UUID subcategoriaId;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Cuestionario() {
        // JPA
    }

    private Cuestionario(UUID subcategoriaId, String nombre) {
        this.subcategoriaId = subcategoriaId;
        this.nombre = nombre;
        this.activo = true;
    }

    public static Cuestionario crear(UUID subcategoriaId, String nombre) {
        return new Cuestionario(subcategoriaId, nombre);
    }

    public void actualizar(String nombre) {
        this.nombre = nombre;
    }

    public void desactivar() {
        this.activo = false;
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNumPreguntas() {
        return numPreguntas;
    }

    public UUID getSubcategoriaId() {
        return subcategoriaId;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
