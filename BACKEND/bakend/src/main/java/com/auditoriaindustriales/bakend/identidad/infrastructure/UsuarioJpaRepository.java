package com.auditoriaindustriales.bakend.identidad.infrastructure;

import com.auditoriaindustriales.bakend.identidad.domain.Usuario;
import com.auditoriaindustriales.bakend.identidad.domain.UsuarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data genera el proxy de esta interfaz e implementa a la vez
 * JpaRepository y el puerto de dominio UsuarioRepository — evita una clase
 * adaptadora adicional que aquí no aporta nada, dado que es un monolito
 * modular y no hay infraestructura intercambiable real.
 */
public interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID>, UsuarioRepository {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findAllByOrderByNombre();

    @Override
    default Optional<Usuario> buscarPorCorreo(String correo) {
        return findByCorreo(correo);
    }

    @Override
    default Optional<Usuario> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default boolean existePorCorreo(String correo) {
        return existsByCorreo(correo);
    }

    @Override
    default List<Usuario> listar() {
        return findAllByOrderByNombre();
    }

    @Override
    default Usuario guardar(Usuario usuario) {
        return save(usuario);
    }
}
