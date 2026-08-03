package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.EnumJson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoAuditoria {
    EN_PROGRESO, FINALIZADA, CANCELADA;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EstadoAuditoria fromJson(String valor) {
        return EnumJson.parse(EstadoAuditoria.class, valor);
    }
}
