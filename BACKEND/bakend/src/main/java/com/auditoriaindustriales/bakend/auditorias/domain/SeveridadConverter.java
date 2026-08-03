package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SeveridadConverter extends LowerCaseEnumConverter<Severidad> {
    public SeveridadConverter() {
        super(Severidad.class);
    }
}
