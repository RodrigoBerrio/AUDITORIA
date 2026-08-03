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
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(length = 50)
    private String icono;

    @Column(name = "es_plantilla", nullable = false)
    private boolean esPlantilla;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Categoria() {
        // JPA
    }

    private Categoria(String nombre, String descripcion, String icono, boolean esPlantilla) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.esPlantilla = esPlantilla;
        this.activo = true;
    }

    public static Categoria crear(String nombre, String descripcion, String icono, boolean esPlantilla) {
        return new Categoria(nombre, descripcion, icono, esPlantilla);
    }

    public void actualizar(String nombre, String descripcion, String icono, boolean esPlantilla) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.esPlantilla = esPlantilla;
    }

    /** Nunca DELETE: el catálogo puede tener historial de auditorías asociado. */
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

    public String getIcono() {
        return icono;
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
