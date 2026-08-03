package com.auditoriaindustriales.bakend.shared.security;

import com.auditoriaindustriales.bakend.shared.domain.Rol;

import java.util.UUID;

/**
 * Principal que queda en el SecurityContext tras validar el JWT.
 * Los servicios de aplicación lo leen vía {@code @AuthenticationPrincipal}
 * para saber quién hace la petición, sin volver a tocar la base de datos.
 */
public record AutenticacionUsuario(UUID usuarioId, String correo, Rol rol) {
}
