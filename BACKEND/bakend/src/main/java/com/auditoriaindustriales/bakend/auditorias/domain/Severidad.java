package com.auditoriaindustriales.bakend.auditorias.domain;

import com.auditoriaindustriales.bakend.shared.domain.EnumJson;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Severidad {
    BAJA, MEDIA, ALTA, CRITICA;

    /**
     * Paleta de 4 pasos verde-amarillo-naranja-rojo; misma fuente para dona, leyenda y PDF.
     * MEDIA y ALTA antes eran dos tonos de naranja casi idénticos (#D4860A/#E67E22, ~9° de
     * separación de matiz) — indistinguibles en la dona de hallazgos. Ahora tienen matices
     * bien separados (ámbar vs. naranja quemado, ~30°+) para que el color solo no obligue a
     * leer la leyenda.
     */
    public String colorHex() {
        return switch (this) {
            case BAJA -> "#2E9E5B";
            case MEDIA -> "#E4A317";
            case ALTA -> "#D9541C";
            case CRITICA -> "#B0281A";
        };
    }

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Severidad fromJson(String valor) {
        return EnumJson.parse(Severidad.class, valor);
    }
}
