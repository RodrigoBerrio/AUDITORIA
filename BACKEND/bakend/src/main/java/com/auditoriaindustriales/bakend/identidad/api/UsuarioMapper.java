package com.auditoriaindustriales.bakend.identidad.api;

import com.auditoriaindustriales.bakend.identidad.api.dto.UsuarioResponse;
import com.auditoriaindustriales.bakend.identidad.domain.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
