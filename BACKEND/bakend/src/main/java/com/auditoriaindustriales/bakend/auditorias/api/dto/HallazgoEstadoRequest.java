package com.auditoriaindustriales.bakend.auditorias.api.dto;

import com.auditoriaindustriales.bakend.auditorias.domain.EstadoHallazgo;
import jakarta.validation.constraints.NotNull;

public record HallazgoEstadoRequest(@NotNull EstadoHallazgo estado) {
}
