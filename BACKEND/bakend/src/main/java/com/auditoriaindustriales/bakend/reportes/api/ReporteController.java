package com.auditoriaindustriales.bakend.reportes.api;

import com.auditoriaindustriales.bakend.reportes.api.dto.ReporteResponse;
import com.auditoriaindustriales.bakend.reportes.application.ReporteService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/api/auditorias/{auditoriaId}/reportes")
    public List<ReporteResponse> listar(@PathVariable UUID auditoriaId) {
        return reporteService.listarPorAuditoria(auditoriaId);
    }

    @GetMapping("/api/reportes/{id}")
    public ReporteResponse obtener(@PathVariable UUID id) {
        return reporteService.obtener(id);
    }

    @PostMapping("/api/auditorias/{auditoriaId}/reportes")
    @ResponseStatus(HttpStatus.CREATED)
    public ReporteResponse generar(@PathVariable UUID auditoriaId, @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return reporteService.generar(auditoriaId, usuario);
    }
}
