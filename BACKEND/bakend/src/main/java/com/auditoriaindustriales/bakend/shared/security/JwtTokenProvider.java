package com.auditoriaindustriales.bakend.shared.security;

import com.auditoriaindustriales.bakend.shared.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** Emite y valida access/refresh tokens. Ambos usan el mismo secreto; el claim "tipo" evita que un refresh token se use como access token. */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_CORREO = "correo";
    private static final String CLAIM_ROL = "rol";
    private static final String CLAIM_TIPO = "tipo";
    private static final String TIPO_ACCESS = "access";
    private static final String TIPO_REFRESH = "refresh";

    private final SecretKey clave;
    private final Duration duracionAccessToken;
    private final Duration duracionRefreshToken;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.access-token-minutes}") long minutosAccess,
            @Value("${app.jwt.refresh-token-days}") long diasRefresh) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.duracionAccessToken = Duration.ofMinutes(minutosAccess);
        this.duracionRefreshToken = Duration.ofDays(diasRefresh);
    }

    public String generarAccessToken(UUID usuarioId, String correo, Rol rol) {
        return construir(usuarioId, correo, rol, TIPO_ACCESS, duracionAccessToken);
    }

    public String generarRefreshToken(UUID usuarioId, String correo, Rol rol) {
        return construir(usuarioId, correo, rol, TIPO_REFRESH, duracionRefreshToken);
    }

    private String construir(UUID usuarioId, String correo, Rol rol, String tipo, Duration duracion) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim(CLAIM_CORREO, correo)
                .claim(CLAIM_ROL, rol.name())
                .claim(CLAIM_TIPO, tipo)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }

    /** @throws JwtException si el token es inválido, está mal firmado o expiró. */
    public Claims parsear(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }

    public boolean esAccessToken(Claims claims) {
        return TIPO_ACCESS.equals(claims.get(CLAIM_TIPO, String.class));
    }

    public boolean esRefreshToken(Claims claims) {
        return TIPO_REFRESH.equals(claims.get(CLAIM_TIPO, String.class));
    }

    public UUID extraerUsuarioId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public Rol extraerRol(Claims claims) {
        return Rol.valueOf(claims.get(CLAIM_ROL, String.class));
    }
}
