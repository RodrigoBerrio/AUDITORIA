package com.auditoriaindustriales.bakend.auditorias.api.dto;

import com.auditoriaindustriales.bakend.auditorias.domain.EstadoHallazgo;
import com.auditoriaindustriales.bakend.auditorias.domain.Severidad;

import java.util.UUID;

public record HallazgoResponse(
        UUID id, UUID auditoriaId, String descripcion, Severidad severidad,
        String accionRecomendada, EstadoHallazgo estado, String area) {
}
