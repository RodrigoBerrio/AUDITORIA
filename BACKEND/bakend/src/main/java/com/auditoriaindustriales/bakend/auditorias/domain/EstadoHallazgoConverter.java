package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoHallazgoConverter extends LowerCaseEnumConverter<EstadoHallazgo> {
    public EstadoHallazgoConverter() {
        super(EstadoHallazgo.class);
    }
}
