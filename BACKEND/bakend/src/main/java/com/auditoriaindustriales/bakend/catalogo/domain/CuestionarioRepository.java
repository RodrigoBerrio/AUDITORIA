package com.auditoriaindustriales.bakend.catalogo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuestionarioRepository {

    Optional<Cuestionario> buscarPorId(UUID id);

    List<Cuestionario> listarPorSubcategoria(UUID subcategoriaId, boolean soloActivas);

    boolean existePorSubcategoriaYNombre(UUID subcategoriaId, String nombre);

    Cuestionario guardar(Cuestionario cuestionario);
}
