package com.auditoriaindustriales.bakend.shared.security;

import com.auditoriaindustriales.bakend.shared.web.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 401/403 en JSON consistente con el resto de la API, nunca la página de
 * login por defecto de Spring Security. Se instancia su propio ObjectMapper
 * (Jackson 2 clásico, vía jjwt-jackson) en vez de inyectar uno de Spring:
 * Spring Boot 4 configura por defecto el nuevo JsonMapper de Jackson 3
 * (tools.jackson.databind), no un bean com.fasterxml.jackson.databind.ObjectMapper —
 * este componente corre fuera del pipeline de Spring MVC (antes de que exista
 * un Authentication), así que de todos modos no puede reusar los
 * HttpMessageConverter normales.
 */
@Component
public class JsonAuthEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        escribir(response, HttpStatus.UNAUTHORIZED, "Se requiere autenticación para acceder a este recurso.", request.getRequestURI());
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación.", request.getRequestURI());
    }

    private void escribir(HttpServletResponse response, HttpStatus status, String mensaje, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), mensaje, path);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
