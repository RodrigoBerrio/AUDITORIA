package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaCrearRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.AuditoriaResponse;
import com.auditoriaindustriales.bakend.auditorias.application.AuditoriaService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditorias")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<AuditoriaResponse> listar() {
        return auditoriaService.listar();
    }

    @GetMapping("/{id}")
    public AuditoriaResponse obtener(@PathVariable UUID id) {
        return auditoriaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuditoriaResponse crear(@Valid @RequestBody AuditoriaCrearRequest request, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return auditoriaService.crear(request.empresaId(), usuario);
    }

    @PostMapping("/{id}/finalizar")
    public AuditoriaResponse finalizar(@PathVariable UUID id, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return auditoriaService.finalizar(id, usuario);
    }

    @PostMapping("/{id}/cancelar")
    public AuditoriaResponse cancelar(@PathVariable UUID id, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return auditoriaService.cancelar(id, usuario);
    }
}
