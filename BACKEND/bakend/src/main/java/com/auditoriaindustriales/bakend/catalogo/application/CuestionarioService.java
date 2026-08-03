package com.auditoriaindustriales.bakend.catalogo.application;

import com.auditoriaindustriales.bakend.catalogo.api.CuestionarioMapper;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CuestionarioRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.CuestionarioResponse;
import com.auditoriaindustriales.bakend.catalogo.domain.Cuestionario;
import com.auditoriaindustriales.bakend.catalogo.domain.CuestionarioRepository;
import com.auditoriaindustriales.bakend.catalogo.domain.SubcategoriaRepository;
import com.auditoriaindustriales.bakend.shared.domain.ConflictException;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CuestionarioService {

    private final CuestionarioRepository cuestionarioRepository;
    private final SubcategoriaRepository subcategoriaRepository;
    private final CuestionarioMapper cuestionarioMapper;

    public CuestionarioService(CuestionarioRepository cuestionarioRepository, SubcategoriaRepository subcategoriaRepository, CuestionarioMapper cuestionarioMapper) {
        this.cuestionarioRepository = cuestionarioRepository;
        this.subcategoriaRepository = subcategoriaRepository;
        this.cuestionarioMapper = cuestionarioMapper;
    }

    @Transactional(readOnly = true)
    public List<CuestionarioResponse> listarPorSubcategoria(UUID subcategoriaId, boolean soloActivas) {
        return cuestionarioRepository.listarPorSubcategoria(subcategoriaId, soloActivas).stream()
                .map(cuestionarioMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CuestionarioResponse obtener(UUID id) {
        return cuestionarioMapper.toResponse(buscarOFallar(id));
    }

    public CuestionarioResponse crear(UUID subcategoriaId, CuestionarioRequest request) {
        subcategoriaRepository.buscarPorId(subcategoriaId).orElseThrow(() -> NotFoundException.of("Subcategoría", subcategoriaId));
        if (cuestionarioRepository.existePorSubcategoriaYNombre(subcategoriaId, request.nombre())) {
            throw new ConflictException("Ya existe un cuestionario con el nombre '" + request.nombre() + "' en esta subcategoría.");
        }
        Cuestionario cuestionario = Cuestionario.crear(subcategoriaId, request.nombre());
        return cuestionarioMapper.toResponse(cuestionarioRepository.guardar(cuestionario));
    }

    public CuestionarioResponse actualizar(UUID id, CuestionarioRequest request) {
        Cuestionario cuestionario = buscarOFallar(id);
        cuestionario.actualizar(request.nombre());
        return cuestionarioMapper.toResponse(cuestionarioRepository.guardar(cuestionario));
    }

    public void desactivar(UUID id) {
        Cuestionario cuestionario = buscarOFallar(id);
        cuestionario.desactivar();
        cuestionarioRepository.guardar(cuestionario);
    }

    private Cuestionario buscarOFallar(UUID id) {
        return cuestionarioRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Cuestionario", id));
    }
}
