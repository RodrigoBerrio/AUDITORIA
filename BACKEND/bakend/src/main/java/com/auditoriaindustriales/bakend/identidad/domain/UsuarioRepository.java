package com.auditoriaindustriales.bakend.identidad.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Puerto de dominio: la capa de aplicación depende de esto, nunca de JpaRepository directamente. */
public interface UsuarioRepository {

    Optional<Usuario> buscarPorCorreo(String correo);

    Optional<Usuario> buscarPorId(UUID id);

    boolean existePorCorreo(String correo);

    List<Usuario> listar();

    Usuario guardar(Usuario usuario);
}
