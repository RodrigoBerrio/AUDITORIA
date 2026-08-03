package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.RespuestaResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.Respuesta;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RespuestaMapper {
    RespuestaResponse toResponse(Respuesta respuesta);
}
