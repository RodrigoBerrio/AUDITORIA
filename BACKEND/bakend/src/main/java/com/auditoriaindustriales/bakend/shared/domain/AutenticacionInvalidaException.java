package com.auditoriaindustriales.bakend.shared.domain;

/** Se traduce a HTTP 401. Credenciales incorrectas, usuario inactivo, o refresh token inválido/expirado. */
public class AutenticacionInvalidaException extends DomainException {

    public AutenticacionInvalidaException(String message) {
        super(message);
    }
}
