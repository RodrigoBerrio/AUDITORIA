package com.auditoriaindustriales.bakend.shared.domain;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolConverter extends LowerCaseEnumConverter<Rol> {

    public RolConverter() {
        super(Rol.class);
    }
}
