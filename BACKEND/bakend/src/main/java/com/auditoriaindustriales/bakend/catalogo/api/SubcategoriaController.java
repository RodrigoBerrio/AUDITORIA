package com.auditoriaindustriales.bakend.catalogo.api;

import com.auditoriaindustriales.bakend.catalogo.api.dto.SubcategoriaRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.SubcategoriaResponse;
import com.auditoriaindustriales.bakend.catalogo.application.SubcategoriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class SubcategoriaController {

    private final SubcategoriaService subcategoriaService;

    public SubcategoriaController(SubcategoriaService subcategoriaService) {
        this.subcategoriaService = subcategoriaService;
    }

    @GetMapping("/api/categorias/{categoriaId}/subcategorias")
    public List<SubcategoriaResponse> listar(@PathVariable UUID categoriaId, @RequestParam(defaultValue = "true") boolean soloActivas) {
        return subcategoriaService.listarPorCategoria(categoriaId, soloActivas);
    }

    @PostMapping("/api/categorias/{categoriaId}/subcategorias")
    @ResponseStatus(HttpStatus.CREATED)
    public SubcategoriaResponse crear(@PathVariable UUID categoriaId, @Valid @RequestBody SubcategoriaRequest request) {
        return subcategoriaService.crear(categoriaId, request);
    }

    @GetMapping("/api/subcategorias/{id}")
    public SubcategoriaResponse obtener(@PathVariable UUID id) {
        return subcategoriaService.obtener(id);
    }

    @PutMapping("/api/subcategorias/{id}")
    public SubcategoriaResponse actualizar(@PathVariable UUID id, @Valid @RequestBody SubcategoriaRequest request) {
        return subcategoriaService.actualizar(id, request);
    }

    @DeleteMapping("/api/subcategorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        subcategoriaService.desactivar(id);
    }
}
