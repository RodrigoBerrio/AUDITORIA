package com.auditoriaindustriales.bakend.shared.domain;

import jakarta.persistence.AttributeConverter;

/**
 * Los CHECK de Postgres usan valores en minúscula ('en_progreso', 'critica'...)
 * pero la convención de Java es enums en mayúscula. EnumType.STRING de JPA
 * serializa con el nombre exacto del enum (mayúsculas) y rompería el CHECK;
 * este converter traduce en ambas direcciones para que el enum Java se pueda
 * escribir en mayúscula sin tocar el esquema.
 */
public abstract class LowerCaseEnumConverter<E extends Enum<E>> implements AttributeConverter<E, String> {

    private final Class<E> tipoEnum;

    protected LowerCaseEnumConverter(Class<E> tipoEnum) {
        this.tipoEnum = tipoEnum;
    }

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Enum.valueOf(tipoEnum, dbData.toUpperCase());
    }
}
