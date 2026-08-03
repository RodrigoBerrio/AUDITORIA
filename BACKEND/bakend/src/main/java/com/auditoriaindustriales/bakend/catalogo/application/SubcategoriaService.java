package com.auditoriaindustriales.bakend.catalogo.application;

import com.auditoriaindustriales.bakend.catalogo.api.SubcategoriaMapper;
import com.auditoriaindustriales.bakend.catalogo.api.dto.SubcategoriaRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.SubcategoriaResponse;
import com.auditoriaindustriales.bakend.catalogo.domain.CategoriaRepository;
import com.auditoriaindustriales.bakend.catalogo.domain.Subcategoria;
import com.auditoriaindustriales.bakend.catalogo.domain.SubcategoriaRepository;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SubcategoriaService {

    private final SubcategoriaRepository subcategoriaRepository;
    private final CategoriaRepository categoriaRepository;
    private final SubcategoriaMapper subcategoriaMapper;

    public SubcategoriaService(SubcategoriaRepository subcategoriaRepository, CategoriaRepository categoriaRepository, SubcategoriaMapper subcategoriaMapper) {
        this.subcategoriaRepository = subcategoriaRepository;
        this.categoriaRepository = categoriaRepository;
        this.subcategoriaMapper = subcategoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<SubcategoriaResponse> listarPorCategoria(UUID categoriaId, boolean soloActivas) {
        return subcategoriaRepository.listarPorCategoria(categoriaId, soloActivas).stream()
                .map(subcategoriaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SubcategoriaResponse obtener(UUID id) {
        return subcategoriaMapper.toResponse(buscarOFallar(id));
    }

    public SubcategoriaResponse crear(UUID categoriaId, SubcategoriaRequest request) {
        categoriaRepository.buscarPorId(categoriaId).orElseThrow(() -> NotFoundException.of("Categoría", categoriaId));
        if (subcategoriaRepository.existePorCategoriaYNombre(categoriaId, request.nombre())) {
            throw new ConflictException("Ya existe una subcategoría con el nombre '" + request.nombre() + "' en esta categoría.");
        }
        Subcategoria subcategoria = Subcategoria.crear(
                categoriaId, request.nombre(), request.descripcion(), request.responsable(), request.esPlantilla());
        return subcategoriaMapper.toResponse(subcategoriaRepository.guardar(subcategoria));
    }

    public SubcategoriaResponse actualizar(UUID id, SubcategoriaRequest request) {
        Subcategoria subcategoria = buscarOFallar(id);
        subcategoria.actualizar(request.nombre(), request.descripcion(), request.responsable(), request.esPlantilla());
        return subcategoriaMapper.toResponse(subcategoriaRepository.guardar(subcategoria));
    }

    public void desactivar(UUID id) {
        Subcategoria subcategoria = buscarOFallar(id);
        subcategoria.desactivar();
        subcategoriaRepository.guardar(subcategoria);
    }

    private Subcategoria buscarOFallar(UUID id) {
        return subcategoriaRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Subcategoría", id));
    }
}
