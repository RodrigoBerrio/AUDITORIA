package com.auditoriaindustriales.bakend.resultados.api.dto;

import com.auditoriaindustriales.bakend.auditorias.domain.EstadoHallazgo;
import com.auditoriaindustriales.bakend.auditorias.domain.Severidad;

import java.util.List;

public record HallazgosResumenResponse(int total, List<ConteoSeveridad> porSeveridad, List<ConteoEstado> porEstado) {

    /** porcentaje ya calculado en backend (0-100, redondeado a 1 decimal) para que la leyenda de la dona no lo recalcule. */
    public record ConteoSeveridad(Severidad severidad, int cantidad, double porcentaje, String colorHex) {
    }

    public record ConteoEstado(EstadoHallazgo estado, int cantidad, double porcentaje) {
    }
}
