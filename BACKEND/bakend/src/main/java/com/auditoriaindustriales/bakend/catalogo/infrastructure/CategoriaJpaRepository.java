package com.auditoriaindustriales.bakend.catalogo.infrastructure;

import com.auditoriaindustriales.bakend.catalogo.domain.Categoria;
import com.auditoriaindustriales.bakend.catalogo.domain.CategoriaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaJpaRepository extends JpaRepository<Categoria, UUID>, CategoriaRepository {

    boolean existsByNombre(String nombre);

    List<Categoria> findByActivoTrueOrderByNombre();

    List<Categoria> findAllByOrderByNombre();

    @Override
    default Optional<Categoria> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Categoria> listar(boolean soloActivas) {
        return soloActivas ? findByActivoTrueOrderByNombre() : findAllByOrderByNombre();
    }

    @Override
    default boolean existePorNombre(String nombre) {
        return existsByNombre(nombre);
    }

    @Override
    default Categoria guardar(Categoria categoria) {
        return save(categoria);
    }
}
