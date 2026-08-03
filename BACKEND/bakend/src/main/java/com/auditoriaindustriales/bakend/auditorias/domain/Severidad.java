package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.EnumJson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Severidad {
    BAJA, MEDIA, ALTA, CRITICA;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Severidad fromJson(String valor) {
        return EnumJson.parse(Severidad.class, valor);
    }
}
