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
@Table(name = "subcategoria")
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "categoria_id", nullable = false)
    private UUID categoriaId;

    @Column(length = 100)
    private String responsable;

    @Column(name = "es_plantilla", nullable = false)
    private boolean esPlantilla;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Subcategoria() {
        // JPA
    }

    private Subcategoria(UUID categoriaId, String nombre, String descripcion, String responsable, boolean esPlantilla) {
        this.categoriaId = categoriaId;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.responsable = responsable;
        this.esPlantilla = esPlantilla;
        this.activo = true;
    }

    public static Subcategoria crear(UUID categoriaId, String nombre, String descripcion, String responsable, boolean esPlantilla) {
        return new Subcategoria(categoriaId, nombre, descripcion, responsable, esPlantilla);
    }

    public void actualizar(String nombre, String descripcion, String responsable, boolean esPlantilla) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.responsable = responsable;
        this.esPlantilla = esPlantilla;
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

    public String getDescripcion() {
        return descripcion;
    }

    public UUID getCategoriaId() {
        return categoriaId;
    }

    public String getResponsable() {
        return responsable;
    }

    public boolean isEsPlantilla() {
        return esPlantilla;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
