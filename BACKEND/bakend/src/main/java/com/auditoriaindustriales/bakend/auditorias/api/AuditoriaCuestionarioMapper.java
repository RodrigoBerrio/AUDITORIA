package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaCuestionarioResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaCuestionario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditoriaCuestionarioMapper {
    AuditoriaCuestionarioResponse toResponse(AuditoriaCuestionario auditoriaCuestionario);
}
