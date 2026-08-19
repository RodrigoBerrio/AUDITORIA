package com.auditoriaindustriales.bakend.empresas.api;

import com.auditoriaindustriales.bakend.empresas.api.dto.EmpresaRequest;
import com.auditoriaindustriales.bakend.empresas.api.dto.EmpresaResponse;
import com.auditoriaindustriales.bakend.empresas.api.dto.HistoricoPuntoResponse;
import com.auditoriaindustriales.bakend.empresas.application.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping
    public List<EmpresaResponse> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public EmpresaResponse obtener(@PathVariable UUID id) {
        return empresaService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmpresaResponse crear(@Valid @RequestBody EmpresaRequest request) {
        return empresaService.crear(request);
    }

    @PutMapping("/{id}")
    public EmpresaResponse actualizar(@PathVariable UUID id, @Valid @RequestBody EmpresaRequest request) {
        return empresaService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        empresaService.eliminar(id);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoPuntoResponse> historico(@PathVariable UUID id) {
        return empresaService.historico(id);
    }
}
