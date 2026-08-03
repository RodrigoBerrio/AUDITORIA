package com.auditoriaindustriales.bakend.identidad.api.dto;

import com.auditoriaindustriales.bakend.shared.domain.Rol;

import java.util.UUID;

/** Misma forma que la interfaz Usuario de domain.ts (camelCase, sin passwordHash). */
public record UsuarioResponse(UUID id, String nombre, String correo, Rol rol, boolean activo) {
}
