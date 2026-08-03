package com.auditoriaindustriales.bakend.shared.domain;

/**
 * Los enums de dominio (Rol, EstadoAuditoria, EstadoCuestionario, Severidad,
 * EstadoHallazgo) deben viajar en JSON en minúscula — domain.ts (el contrato
 * de referencia del frontend) los declara así, calcados de los valores del
 * CHECK de Postgres. Sin esto, Jackson serializa el nombre del enum tal cual
 * (mayúscula), que es lo que usa LowerCaseEnumConverter para la base de
 * datos, pero no lo que espera el frontend por la API. Ambos convierten en
 * minúscula por la misma razón; son mecanismos distintos (JPA vs Jackson)
 * porque resuelven capas distintas (columna de BD vs contrato HTTP).
 */
public final class EnumJson {

    private EnumJson() {
    }

    public static <E extends Enum<E>> E parse(Class<E> tipo, String valor) {
        return Enum.valueOf(tipo, valor.toUpperCase());
    }
}
