package com.auditoriaindustriales.bakend.empresas.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Misma forma que la interfaz Empresa de domain.ts. */
public record EmpresaResponse(
        UUID id,
        String razonSocial,
        String nit,
        String sector,
        Integer numEmpleados,
        String ciudad,
        String departamento,
        String codigoPostal,
        String contacto,
        String telefono,
        String correo,
        String descripcion,
        long auditoriasRealizadas,
        LocalDate ultimaVisita) {
}
