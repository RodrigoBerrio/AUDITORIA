package com.auditoriaindustriales.bakend.shared.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Roles de usuario ({@code usuario.rol}). Vive en el kernel compartido —no
 * en el módulo identidad— porque seguridad y autorización lo necesitan en
 * todos los módulos, no solo en identidad.
 *
 * No incluye 'cliente': ese rol es una maqueta de UI en el frontend
 * (RolSesion), no un valor real del CHECK de usuario.rol. Agregarlo aquí
 * el día que sí se implemente es un cambio de un solo enum + migración del
 * CHECK, sin tocar el resto de la autorización.
 */
public enum Rol {
    AUDITOR, ADMIN, SUPERVISOR;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Rol fromJson(String valor) {
        return EnumJson.parse(Rol.class, valor);
    }
}
