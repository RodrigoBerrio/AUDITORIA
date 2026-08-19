package com.auditoriaindustriales.bakend.empresas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record HistoricoPuntoResponse(LocalDate fechaFin, BigDecimal puntajeGlobal) {
}
