package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.LowerCaseEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoAuditoriaConverter extends LowerCaseEnumConverter<EstadoAuditoria> {
    public EstadoAuditoriaConverter() {
        super(EstadoAuditoria.class);
    }
}
