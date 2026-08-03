package com.auditoriaindustriales.bakend.catalogo.api;

import com.auditoriaindustriales.bakend.catalogo.api.dto.CuestionarioResponse;
import com.auditoriaindustriales.bakend.catalogo.domain.Cuestionario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CuestionarioMapper {
    CuestionarioResponse toResponse(Cuestionario cuestionario);
}
