package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.EnumJson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** El orden de declaración importa: solo se permite avanzar (ordinal mayor), nunca retroceder. */
public enum EstadoCuestionario {
    PENDIENTE, EN_PROGRESO, COMPLETADO;

    public boolean esTransicionValidaHacia(EstadoCuestionario destino) {
        return destino.ordinal() > this.ordinal();
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EstadoCuestionario fromJson(String valor) {
        return EnumJson.parse(EstadoCuestionario.class, valor);
    }
}
