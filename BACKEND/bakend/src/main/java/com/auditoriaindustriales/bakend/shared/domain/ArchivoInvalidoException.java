package com.auditoriaindustriales.bakend.shared.domain;

/** Se traduce a HTTP 400. Archivo demasiado grande o de un tipo no permitido. */
public class ArchivoInvalidoException extends DomainException {

    public ArchivoInvalidoException(String message) {
        super(message);
    }
}
