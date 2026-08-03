package com.auditoriaindustriales.bakend.auditorias.api.dto;

import com.auditoriaindustriales.bakend.auditorias.domain.Severidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HallazgoRequest(
        @NotBlank @Size(max = 500) String descripcion,
        @NotNull Severidad severidad,
        @Size(max = 500) String accionRecomendada,
        @Size(max = 100) String area) {
}
