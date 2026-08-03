package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuditoriaMapper {
    AuditoriaResponse toResponse(Auditoria auditoria);
}
