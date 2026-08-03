package com.auditoriaindustriales.bakend.catalogo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubcategoriaRepository {

    Optional<Subcategoria> buscarPorId(UUID id);

    List<Subcategoria> listarPorCategoria(UUID categoriaId, boolean soloActivas);

    boolean existePorCategoriaYNombre(UUID categoriaId, String nombre);

    Subcategoria guardar(Subcategoria subcategoria);
}
