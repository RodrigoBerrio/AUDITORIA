package com.auditoriaindustriales.bakend.auditorias.api;

import com.auditoriaindustriales.bakend.auditorias.api.dto.EvidenciaResponse;
import com.auditoriaindustriales.bakend.auditorias.api.dto.EvidenciaSubidaRequest;
import com.auditoriaindustriales.bakend.auditorias.application.EvidenciaService;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditorias/{auditoriaId}/evidencias")
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    public EvidenciaController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping
    public List<EvidenciaResponse> listar(@PathVariable UUID auditoriaId) {
        return evidenciaService.listarPorAuditoria(auditoriaId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenciaResponse subir(
            @PathVariable UUID auditoriaId,
            @RequestPart("archivo") MultipartFile archivo,
            @Valid @RequestPart("metadata") EvidenciaSubidaRequest metadata,
            @AuthenticationPrincipal AutenticacionUsuario usuario) {
        return evidenciaService.subir(auditoriaId, archivo, metadata, usuario);
    }
}
