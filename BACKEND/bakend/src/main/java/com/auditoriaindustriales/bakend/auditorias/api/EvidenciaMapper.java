package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.EvidenciaResponse;
import com.auditoriaindustriales.bakend.auditorias.domain.EvidenciaFotografica;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EvidenciaMapper {
    EvidenciaResponse toResponse(EvidenciaFotografica evidencia);
}
