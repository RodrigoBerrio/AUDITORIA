package com.auditoriaindustriales.bakend.catalogo.api;

import com.auditoriaindustriales.bakend.catalogo.api.dto.CuestionarioRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CuestionarioResponse;
import com.auditoriaindustriales.bakend.catalogo.application.CuestionarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class CuestionarioController {

    private final CuestionarioService cuestionarioService;

    public CuestionarioController(CuestionarioService cuestionarioService) {
        this.cuestionarioService = cuestionarioService;
    }

    @GetMapping("/api/subcategorias/{subcategoriaId}/cuestionarios")
    public List<CuestionarioResponse> listar(@PathVariable UUID subcategoriaId, @RequestParam(defaultValue = "true") boolean soloActivas) {
        return cuestionarioService.listarPorSubcategoria(subcategoriaId, soloActivas);
    }

    @PostMapping("/api/subcategorias/{subcategoriaId}/cuestionarios")
    @ResponseStatus(HttpStatus.CREATED)
    public CuestionarioResponse crear(@PathVariable UUID subcategoriaId, @Valid @RequestBody CuestionarioRequest request) {
        return cuestionarioService.crear(subcategoriaId, request);
    }

    @GetMapping("/api/cuestionarios/{id}")
    public CuestionarioResponse obtener(@PathVariable UUID id) {
        return cuestionarioService.obtener(id);
    }

    @PutMapping("/api/cuestionarios/{id}")
    public CuestionarioResponse actualizar(@PathVariable UUID id, @Valid @RequestBody CuestionarioRequest request) {
        return cuestionarioService.actualizar(id, request);
    }

    @DeleteMapping("/api/cuestionarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        cuestionarioService.desactivar(id);
    }
}
