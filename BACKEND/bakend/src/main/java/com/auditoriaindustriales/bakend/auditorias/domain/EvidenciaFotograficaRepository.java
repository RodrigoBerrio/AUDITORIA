package com.auditoriaindustriales.bakend.auditorias.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenciaFotograficaRepository {

    Optional<EvidenciaFotografica> buscarPorClienteUuid(UUID clienteUuid);

    List<EvidenciaFotografica> listarPorAuditoria(UUID auditoriaId);

    EvidenciaFotografica guardar(EvidenciaFotografica evidencia);
}
