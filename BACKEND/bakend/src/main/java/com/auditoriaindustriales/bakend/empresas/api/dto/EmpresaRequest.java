package com.auditoriaindustriales.bakend.empresas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(
        @NotBlank @Size(max = 150) String razonSocial,
        @NotBlank @Size(max = 20) String nit,
        @Size(max = 100) String sector,
        @PositiveOrZero Integer numEmpleados,
        @Size(max = 100) String ciudad,
        @Size(max = 100) String departamento,
        @Size(max = 10) String codigoPostal,
        @Size(max = 100) String contacto,
        @Size(max = 20) String telefono,
        @Email String correo,
        @Size(max = 500) String descripcion) {
}
