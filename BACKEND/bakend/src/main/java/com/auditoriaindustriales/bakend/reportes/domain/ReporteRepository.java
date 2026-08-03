package com.auditoriaindustriales.bakend.reportes.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReporteRepository {

    Optional<Reporte> buscarPorId(UUID id);

    List<Reporte> listarPorAuditoria(UUID auditoriaId);

    Reporte guardar(Reporte reporte);
}
