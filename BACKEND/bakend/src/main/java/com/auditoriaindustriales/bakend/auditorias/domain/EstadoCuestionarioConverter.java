package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoCuestionarioConverter extends LowerCaseEnumConverter<EstadoCuestionario> {
    public EstadoCuestionarioConverter() {
        super(EstadoCuestionario.class);
    }
}
