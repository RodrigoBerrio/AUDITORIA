package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaCuestionarioCrearRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaCuestionarioResponse;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaCuestionarioService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class AuditoriaCuestionarioController {

    private final AuditoriaCuestionarioService auditoriaCuestionarioService;

    public AuditoriaCuestionarioController(AuditoriaCuestionarioService auditoriaCuestionarioService) {
        this.auditoriaCuestionarioService = auditoriaCuestionarioService;
    }

    @GetMapping("/api/auditorias/{auditoriaId}/cuestionarios")
    public List<AuditoriaCuestionarioResponse> listar(@PathVariable UUID auditoriaId) {
        return auditoriaCuestionarioService.listarPorAuditoria(auditoriaId);
    }

    @PostMapping("/api/auditorias/{auditoriaId}/cuestionarios")
    @ResponseStatus(HttpStatus.CREATED)
    public AuditoriaCuestionarioResponse aplicar(
            @PathVariable UUID auditoriaId,
            @Valid @RequestBody AuditoriaCuestionarioCrearRequest request,
            @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return auditoriaCuestionarioService.aplicarCuestionario(auditoriaId, request.cuestionarioId(), usuario);
    }

    @PostMapping("/api/auditoria-cuestionarios/{id}/completar")
    public AuditoriaCuestionarioResponse completar(@PathVariable UUID id, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return auditoriaCuestionarioService.completar(id, usuario);
    }
}
