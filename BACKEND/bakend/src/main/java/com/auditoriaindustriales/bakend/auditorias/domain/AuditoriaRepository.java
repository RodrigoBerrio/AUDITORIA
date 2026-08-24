package com.auditoriaindustriales.bakend.auditorias.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditoriaRepository {

    Optional<Auditoria> buscarPorId(UUID id);

    List<Auditoria> listar();

    Auditoria guardar(Auditoria auditoria);

    Optional<Auditoria> buscarEnProgresoPorEmpresa(UUID empresaId);
}
