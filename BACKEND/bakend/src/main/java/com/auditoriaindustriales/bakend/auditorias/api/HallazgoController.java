package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoEstadoRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoRequest;
import com.auditoriaindustriales.bakend.auditorias.api.dto.HallazgoResponse;
import com.auditoriaindustriales.bakend.auditorias.application.HallazgoService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class HallazgoController {

    private final HallazgoService hallazgoService;

    public HallazgoController(HallazgoService hallazgoService) {
        this.hallazgoService = hallazgoService;
    }

    @GetMapping("/api/auditorias/{auditoriaId}/hallazgos")
    public List<HallazgoResponse> listar(@PathVariable UUID auditoriaId) {
        return hallazgoService.listarPorAuditoria(auditoriaId);
    }

    @PostMapping("/api/auditorias/{auditoriaId}/hallazgos")
    @ResponseStatus(HttpStatus.CREATED)
    public HallazgoResponse crear(
            @PathVariable UUID auditoriaId,
            @Valid @RequestBody HallazgoRequest request,
            @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return hallazgoService.crear(auditoriaId, request, usuario);
    }

    @PutMapping("/api/hallazgos/{id}")
    public HallazgoResponse actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody HallazgoRequest request,
            @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return hallazgoService.actualizar(id, request, usuario);
    }

    @PatchMapping("/api/hallazgos/{id}/estado")
    public HallazgoResponse cambiarEstado(
            @PathVariable UUID id,
            @Valid @RequestBody HallazgoEstadoRequest request,
            @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return hallazgoService.cambiarEstado(id, request.estado(), usuario);
    }
}
