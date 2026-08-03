package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.EnumJson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** El orden de declaración importa: solo se permite avanzar (ordinal mayor), nunca retroceder. */
public enum EstadoHallazgo {
    ABIERTO, EN_TRATAMIENTO, CERRADO;

    public boolean esTransicionValidaHacia(EstadoHallazgo destino) {
        return destino.ordinal() > this.ordinal();
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EstadoHallazgo fromJson(String valor) {
        return EnumJson.parse(EstadoHallazgo.class, valor);
    }
}
