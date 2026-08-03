package com.auditoriaindustriales.bakend.shared.domain;

/** Se traduce a HTTP 403. El usuario está autenticado pero no autorizado para esta operación puntual. */
public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(message);
    }
}
