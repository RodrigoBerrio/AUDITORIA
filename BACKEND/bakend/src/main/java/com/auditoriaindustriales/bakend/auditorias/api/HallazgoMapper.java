package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Hallazgo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HallazgoMapper {
    HallazgoResponse toResponse(Hallazgo hallazgo);
}
