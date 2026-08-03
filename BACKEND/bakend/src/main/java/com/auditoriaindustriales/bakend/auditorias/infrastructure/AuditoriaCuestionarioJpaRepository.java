package com.auditoriaindustriales.bakend.auditorias.infrastructure;

import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaCuestionario;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaCuestionarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditoriaCuestionarioJpaRepository extends JpaRepository<AuditoriaCuestionario, UUID>, AuditoriaCuestionarioRepository {

    List<AuditoriaCuestionario> findByAuditoriaId(UUID auditoriaId);

    boolean existsByAuditoriaIdAndCuestionarioId(UUID auditoriaId, UUID cuestionarioId);

    @Override
    default Optional<AuditoriaCuestionario> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<AuditoriaCuestionario> listarPorAuditoria(UUID auditoriaId) {
        return findByAuditoriaId(auditoriaId);
    }

    @Override
    default boolean existePorAuditoriaYCuestionario(UUID auditoriaId, UUID cuestionarioId) {
        return existsByAuditoriaIdAndCuestionarioId(auditoriaId, cuestionarioId);
    }

    @Override
    default AuditoriaCuestionario guardar(AuditoriaCuestionario auditoriaCuestionario) {
        return save(auditoriaCuestionario);
    }
}
