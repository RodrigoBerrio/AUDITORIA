package com.auditoriaindustriales.bakend.shared.web;

import com.auditoriaindustriales.bakend.shared.domain.ArchivoInvalidoException;
import com.auditoriaindustriales.bakend.shared.domain.AutenticacionInvalidaException;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.ForbiddenException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce toda excepción (de dominio o de la base de datos) a JSON consistente.
 * En particular, el trigger trg_proteger_texto_pregunta llega aquí como una
 * PSQLException con SQLState P0001 (RAISE EXCEPTION de Postgres), y las
 * violaciones UNIQUE (23505) de las tablas puente, en vez de dejar pasar el
 * stack trace de Hibernate.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String SQLSTATE_RAISE_EXCEPTION = "P0001";
    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";
    private static final String SQLSTATE_FOREIGN_KEY_VIOLATION = "23503";

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest req) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), req);
    }

    @ExceptionHandler(AutenticacionInvalidaException.class)
    public ResponseEntity<ApiErrorResponse> handleAutenticacionInvalida(AutenticacionInvalidaException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(ArchivoInvalidoException.class)
    public ResponseEntity<ApiErrorResponse> handleArchivoInvalido(ArchivoInvalidoException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ApiErrorResponse.CampoInvalido> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiErrorResponse.CampoInvalido(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(ApiErrorResponse.ofValidacion(req.getRequestURI(), errores));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleIntegridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        PSQLException psql = findPSQLException(ex);
        if (psql != null && SQLSTATE_UNIQUE_VIOLATION.equals(psql.getSQLState())) {
            return build(HttpStatus.CONFLICT, "El registro ya existe (violación de unicidad).", req);
        }
        if (psql != null && SQLSTATE_FOREIGN_KEY_VIOLATION.equals(psql.getSQLState())) {
            // 23503 cubre dos casos distintos con el mismo SQLState: borrar un
            // padre que todavía tiene hijos (ON DELETE RESTRICT), o insertar/
            // actualizar apuntando a un padre que ya no existe. Los servicios
            // validan la existencia del padre antes de escribir, así que en
            // operación normal esto solo debería dispararse en el caso RESTRICT
            // o en una carrera rara; de ahí el mensaje neutral.
            return build(HttpStatus.CONFLICT, "La operación viola una relación con otro registro (referencia inexistente o registro con datos relacionados).", req);
        }
        log.error("Violación de integridad no reconocida", ex);
        return build(HttpStatus.CONFLICT, "La operación viola una restricción de integridad de datos.", req);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDataAccess(DataAccessException ex, HttpServletRequest req) {
        PSQLException psql = findPSQLException(ex);
        if (psql != null && SQLSTATE_RAISE_EXCEPTION.equals(psql.getSQLState())) {
            // Mensaje de negocio definido en el trigger de Postgres (ej. pregunta con respuestas).
            return build(HttpStatus.CONFLICT, psql.getServerErrorMessage().getMessage(), req);
        }
        log.error("Error de acceso a datos no manejado explícitamente", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al acceder a los datos.", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenerico(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error inesperado.", req);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message, HttpServletRequest req) {
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), message, req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private PSQLException findPSQLException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof PSQLException psql) {
                return psql;
            }
            current = current.getCause();
        }
        return null;
    }
}
