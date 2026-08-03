package com.auditoriaindustriales.bakend.catalogo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository {

    Optional<Categoria> buscarPorId(UUID id);

    List<Categoria> listar(boolean soloActivas);

    boolean existePorNombre(String nombre);

    Categoria guardar(Categoria categoria);
}
