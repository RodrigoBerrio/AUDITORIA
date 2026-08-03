package com.auditoriaindustriales.bakend.shared.domain;

/** Raíz de las excepciones de negocio. Nunca se traduce a un 500 con stack trace expuesto. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
