package com.auditoriaindustriales.bakend.identidad.application;

import com.auditoriaindustriales.bakend.identidad.api.UsuarioMapper;
import com.auditoriaindustriales.bakend.identidad.api.dto.TokenResponse;
import com.auditoriaindustriales.bakend.identidad.domain.Usuario;
import com.auditoriaindustriales.bakend.identidad.domain.UsuarioRepository;
import com.auditoriaindustriales.bakend.shared.domain.AutenticacionInvalidaException;
import com.auditoriaindustriales.bakend.shared.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioMapper usuarioMapper;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioMapper = usuarioMapper;
    }

    public TokenResponse login(String correo, String password) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(correo)
                .orElseThrow(() -> new AutenticacionInvalidaException("Correo o contraseña incorrectos."));

        if (!usuario.isActivo()) {
            throw new AutenticacionInvalidaException("El usuario está desactivado.");
        }
        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new AutenticacionInvalidaException("Correo o contraseña incorrectos.");
        }

        return emitirTokens(usuario);
    }

    public TokenResponse refrescar(String refreshToken) {
        try {
            var claims = jwtTokenProvider.parsear(refreshToken);
            if (!jwtTokenProvider.esRefreshToken(claims)) {
                throw new AutenticacionInvalidaException("El token no es un refresh token válido.");
            }

            Usuario usuario = usuarioRepository.buscarPorId(jwtTokenProvider.extraerUsuarioId(claims))
                    .orElseThrow(() -> new AutenticacionInvalidaException("El usuario ya no existe."));
            if (!usuario.isActivo()) {
                throw new AutenticacionInvalidaException("El usuario está desactivado.");
            }

            return emitirTokens(usuario);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AutenticacionInvalidaException("Refresh token inválido o expirado.");
        }
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        String access = jwtTokenProvider.generarAccessToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());
        String refresh = jwtTokenProvider.generarRefreshToken(usuario.getId(), usuario.getCorreo(), usuario.getRol());
        return TokenResponse.of(access, refresh, usuarioMapper.toResponse(usuario));
    }
}
