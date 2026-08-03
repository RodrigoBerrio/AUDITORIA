package com.auditoriaindustriales.bakend.empresas.domain;

import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepository {

    Optional<Empresa> buscarPorId(UUID id);

    boolean existePorNit(String nit);

    Empresa guardar(Empresa empresa);

    void eliminar(Empresa empresa);
}
