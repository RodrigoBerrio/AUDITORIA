package com.auditoriaindustriales.bakend.catalogo.api;

import com.auditoriaindustriales.bakend.catalogo.api.dto.SubcategoriaResponse;
import com.auditoriaindustriales.bakend.catalogo.domain.Subcategoria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubcategoriaMapper {
    SubcategoriaResponse toResponse(Subcategoria subcategoria);
}
