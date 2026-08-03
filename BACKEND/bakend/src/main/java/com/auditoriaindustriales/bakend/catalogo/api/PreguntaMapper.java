package com.auditoriaindustriales.bakend.catalogo.api;

import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaResponse;
import com.auditoriaindustriales.bakend.catalogo.domain.Pregunta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PreguntaMapper {
    PreguntaResponse toResponse(Pregunta pregunta);
}
