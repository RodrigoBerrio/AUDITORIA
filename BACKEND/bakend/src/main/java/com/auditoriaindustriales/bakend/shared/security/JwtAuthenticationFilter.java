package com.auditoriaindustriales.bakend.shared.security;

import com.auditoriaindustriales.bakend.shared.domain.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autenticación stateless: si el header trae un access token válido, arma el
 * Authentication directamente desde los claims del JWT (sin ir a la base de
 * datos en cada request). Si no hay token, el token es un refresh token, o
 * es inválido/expiró, simplemente deja la petición sin autenticar — es
 * SecurityConfig quien decide si esa ruta exige autenticación.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIJO_BEARER)) {
            String token = header.substring(PREFIJO_BEARER.length());
            try {
                Claims claims = jwtTokenProvider.parsear(token);
                if (jwtTokenProvider.esAccessToken(claims)) {
                    autenticar(claims);
                }
            } catch (JwtException | IllegalArgumentException ex) {
                log.debug("Token JWT inválido o expirado: {}", ex.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private void autenticar(Claims claims) {
        Rol rol = jwtTokenProvider.extraerRol(claims);
        AutenticacionUsuario principal = new AutenticacionUsuario(
                jwtTokenProvider.extraerUsuarioId(claims),
                claims.get("correo", String.class),
                rol);
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));

        var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
