package com.auditoriaindustriales.bakend.empresas.api;

import com.auditoriaindustriales.bakend.empresas.api.dto.EmpresaResponse;
import com.auditoriaindustriales.bakend.empresas.domain.Empresa;
import com.auditoriaindustriales.bakend.empresas.infrastructure.EmpresaListadoRow;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    EmpresaResponse toResponse(EmpresaListadoRow row);

    @Mapping(target = "auditoriasRealizadas", constant = "0L")
    @Mapping(target = "ultimaVisita", ignore = true)
    EmpresaResponse toResponseNueva(Empresa empresa);
}
