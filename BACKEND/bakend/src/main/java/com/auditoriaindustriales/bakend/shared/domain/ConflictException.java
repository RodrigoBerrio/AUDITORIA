package com.auditoriaindustriales.bakend.shared.domain;

/**
 * Se traduce a HTTP 409. Cubre tanto violaciones de UNIQUE/constraint de
 * Postgres traducidas por la capa de aplicación, como transiciones de
 * estado inválidas detectadas en el dominio antes de llegar a la base.
 */
public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message);
    }
}
