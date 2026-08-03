package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.RespuestaRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.RespuestaResponse;
import com.auditoriaindustriales.bakend.auditorias.application.RespuestaService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria-cuestionarios/{auditoriaCuestionarioId}/respuestas")
public class RespuestaController {

    private final RespuestaService respuestaService;

    public RespuestaController(RespuestaService respuestaService) {
        this.respuestaService = respuestaService;
    }

    @GetMapping
    public List<RespuestaResponse> listar(@PathVariable UUID auditoriaCuestionarioId) {
        return respuestaService.listarPorAuditoriaCuestionario(auditoriaCuestionarioId);
    }

    /** Upsert: registra la respuesta o edita la existente para esa pregunta. */
    @PutMapping
    public RespuestaResponse responder(
            @PathVariable UUID auditoriaCuestionarioId,
            @Valid @RequestBody RespuestaRequest request,
            @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return respuestaService.responder(auditoriaCuestionarioId, request, usuario);
    }
}
