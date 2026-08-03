package com.auditoriaindustriales.bakend.reportes.api;

import com.auditoriaindustriales.bakend.reportes.api.dto.ReporteResponse;
import com.auditoriaindustriales.bakend.reportes.domain.Reporte;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReporteMapper {
    ReporteResponse toResponse(Reporte reporte);
}
