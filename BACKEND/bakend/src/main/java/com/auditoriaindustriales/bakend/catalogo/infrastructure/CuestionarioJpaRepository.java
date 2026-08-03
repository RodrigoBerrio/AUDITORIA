package com.auditoriaindustriales.bakend.catalogo.infrastructure;

import com.auditoriaindustriales.bakend.catalogo.domain.Cuestionario;
import com.auditoriaindustriales.bakend.catalogo.domain.CuestionarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CuestionarioJpaRepository extends JpaRepository<Cuestionario, UUID>, CuestionarioRepository {

    boolean existsBySubcategoriaIdAndNombre(UUID subcategoriaId, String nombre);

    List<Cuestionario> findBySubcategoriaIdAndActivoTrueOrderByNombre(UUID subcategoriaId);

    List<Cuestionario> findBySubcategoriaIdOrderByNombre(UUID subcategoriaId);

    @Override
    default Optional<Cuestionario> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Cuestionario> listarPorSubcategoria(UUID subcategoriaId, boolean soloActivas) {
        return soloActivas
                ? findBySubcategoriaIdAndActivoTrueOrderByNombre(subcategoriaId)
                : findBySubcategoriaIdOrderByNombre(subcategoriaId);
    }

    @Override
    default boolean existePorSubcategoriaYNombre(UUID subcategoriaId, String nombre) {
        return existsBySubcategoriaIdAndNombre(subcategoriaId, nombre);
    }

    @Override
    default Cuestionario guardar(Cuestionario cuestionario) {
        return save(cuestionario);
    }
}
