package com.auditoriaindustriales.bakend.auditorias.infrastructure;

import com.auditoriaindustriales.bakend.auditorias.domain.EvidenciaFotografica;
import com.auditoriaindustriales.bakend.auditorias.domain.EvidenciaFotograficaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenciaFotograficaJpaRepository extends JpaRepository<EvidenciaFotografica, UUID>, EvidenciaFotograficaRepository {

    Optional<EvidenciaFotografica> findByClienteUuid(UUID clienteUuid);

    List<EvidenciaFotografica> findByAuditoriaId(UUID auditoriaId);

    @Override
    default Optional<EvidenciaFotografica> buscarPorClienteUuid(UUID clienteUuid) {
        return findByClienteUuid(clienteUuid);
    }

    @Override
    default List<EvidenciaFotografica> listarPorAuditoria(UUID auditoriaId) {
        return findByAuditoriaId(auditoriaId);
    }

    @Override
    default EvidenciaFotografica guardar(EvidenciaFotografica evidencia) {
        return save(evidencia);
    }
}
