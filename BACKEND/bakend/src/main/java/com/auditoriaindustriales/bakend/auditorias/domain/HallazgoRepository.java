package com.auditoriaindustriales.bakend.auditorias.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HallazgoRepository {

    Optional<Hallazgo> buscarPorId(UUID id);

    List<Hallazgo> listarPorAuditoria(UUID auditoriaId);

    Hallazgo guardar(Hallazgo hallazgo);
}
