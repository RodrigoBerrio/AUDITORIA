package com.auditoriaindustriales.bakend.catalogo.infrastructure;

import com.auditoriaindustriales.bakend.catalogo.domain.Subcategoria;
import com.auditoriaindustriales.bakend.catalogo.domain.SubcategoriaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubcategoriaJpaRepository extends JpaRepository<Subcategoria, UUID>, SubcategoriaRepository {

    boolean existsByCategoriaIdAndNombre(UUID categoriaId, String nombre);

    List<Subcategoria> findByCategoriaIdAndActivoTrueOrderByNombre(UUID categoriaId);

    List<Subcategoria> findByCategoriaIdOrderByNombre(UUID categoriaId);

    @Override
    default Optional<Subcategoria> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Subcategoria> listarPorCategoria(UUID categoriaId, boolean soloActivas) {
        return soloActivas
                ? findByCategoriaIdAndActivoTrueOrderByNombre(categoriaId)
                : findByCategoriaIdOrderByNombre(categoriaId);
    }

    @Override
    default boolean existePorCategoriaYNombre(UUID categoriaId, String nombre) {
        return existsByCategoriaIdAndNombre(categoriaId, nombre);
    }

    @Override
    default Subcategoria guardar(Subcategoria subcategoria) {
        return save(subcategoria);
    }
}
