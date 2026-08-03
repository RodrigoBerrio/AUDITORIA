package com.auditoriaindustriales.bakend.identidad.api.dto;

public record TokenResponse(String accessToken, String refreshToken, String tipo, UsuarioResponse usuario) {

    public static TokenResponse of(String accessToken, String refreshToken, UsuarioResponse usuario) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", usuario);
    }
}
