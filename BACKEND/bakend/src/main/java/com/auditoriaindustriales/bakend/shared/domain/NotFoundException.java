package com.auditoriaindustriales.bakend.shared.domain;

/** Se traduce a HTTP 404. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entidad, Object id) {
        return new NotFoundException("%s no encontrado(a) con id %s".formatted(entidad, id));
    }
}
