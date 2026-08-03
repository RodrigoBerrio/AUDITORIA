package com.auditoriaindustriales.bakend.empresas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * auditoriasRealizadas y ultimaVisita (que domain.ts sí expone) NO son
 * atributos de este agregado: son una proyección de solo lectura que arma
 * la capa de infraestructura consultando auditoria por empresa_id — nunca
 * columnas nuevas en esta entidad.
 */
@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 20)
    private String nit;

    @Column(length = 100)
    private String sector;

    @Column(name = "num_empleados")
    private Integer numEmpleados;

    @Column(length = 100)
    private String ciudad;

    @Column(length = 100)
    private String departamento;

    @Column(name = "codigo_postal", length = 10)
    private String codigoPostal;

    @Column(length = 100)
    private String contacto;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String correo;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private Instant creadoEn;

    protected Empresa() {
        // JPA
    }

    private Empresa(String razonSocial, String nit) {
        this.razonSocial = razonSocial;
        this.nit = nit;
    }

    public static Empresa crear(String razonSocial, String nit) {
        return new Empresa(razonSocial, nit);
    }

    public void actualizarDatos(
            String razonSocial, String sector, Integer numEmpleados, String ciudad, String departamento,
            String codigoPostal, String contacto, String telefono, String correo, String descripcion) {
        this.razonSocial = razonSocial;
        this.sector = sector;
        this.numEmpleados = numEmpleados;
        this.ciudad = ciudad;
        this.departamento = departamento;
        this.codigoPostal = codigoPostal;
        this.contacto = contacto;
        this.telefono = telefono;
        this.correo = correo;
        this.descripcion = descripcion;
    }

    public UUID getId() {
        return id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public String getNit() {
        return nit;
    }

    public String getSector() {
        return sector;
    }

    public Integer getNumEmpleados() {
        return numEmpleados;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public String getContacto() {
        return contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
