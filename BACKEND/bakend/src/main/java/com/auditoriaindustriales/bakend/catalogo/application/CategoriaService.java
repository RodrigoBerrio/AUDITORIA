package com.auditoriaindustriales.bakend.catalogo.application;

import com.auditoriaindustriales.bakend.catalogo.api.CategoriaMapper;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CategoriaRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CategoriaResponse;
import com.auditoriaindustriales.bakend.catalogo.domain.Categoria;
import com.auditoriaindustriales.bakend.catalogo.domain.CategoriaRepository;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar(boolean soloActivas) {
        return categoriaRepository.listar(soloActivas).stream().map(categoriaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(UUID id) {
        return categoriaMapper.toResponse(buscarOFallar(id));
    }

    public CategoriaResponse crear(CategoriaRequest request) {
        if (categoriaRepository.existePorNombre(request.nombre())) {
            throw new ConflictException("Ya existe una categoría con el nombre '" + request.nombre() + "'.");
        }
        Categoria categoria = Categoria.crear(request.nombre(), request.descripcion(), request.icono(), request.esPlantilla());
        return categoriaMapper.toResponse(categoriaRepository.guardar(categoria));
    }

    public CategoriaResponse actualizar(UUID id, CategoriaRequest request) {
        Categoria categoria = buscarOFallar(id);
        categoria.actualizar(request.nombre(), request.descripcion(), request.icono(), request.esPlantilla());
        return categoriaMapper.toResponse(categoriaRepository.guardar(categoria));
    }

    /** Nunca DELETE físico: puede tener subcategorías/cuestionarios/preguntas con historial. */
    public void desactivar(UUID id) {
        Categoria categoria = buscarOFallar(id);
        categoria.desactivar();
        categoriaRepository.guardar(categoria);
    }

    private Categoria buscarOFallar(UUID id) {
        return categoriaRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Categoría", id));
    }
}
