package com.auditoriaindustriales.bakend.empresas.infrastructure;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Proyección nativa de empresa + agregación de auditoria.auditoriasRealizadas/
 * ultimaVisita. Es una consulta de solo lectura entre tablas de dos módulos
 * distintos (empresas y auditorias); se resuelve con SQL nativo sobre las
 * tablas —nunca importando la entidad Auditoria del otro módulo— porque en
 * un monolito modular la base de datos compartida ya es el punto de
 * integración real, y no tiene sentido fingir un límite que no existe a
 * nivel de infraestructura solo para evitar un JOIN de solo lectura.
 */
public interface EmpresaListadoRow {
    UUID getId();
    String getRazonSocial();
    String getNit();
    String getSector();
    Integer getNumEmpleados();
    String getCiudad();
    String getDepartamento();
    String getCodigoPostal();
    String getContacto();
    String getTelefono();
    String getCorreo();
    String getDescripcion();
    Long getAuditoriasRealizadas();
    LocalDate getUltimaVisita();
}
