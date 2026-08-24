package com.auditoriaindustriales.bakend.auditorias.infrastructure;

import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import com.auditoriaindustriales.bakend.auditorias.domain.AuditoriaRepository;
import com.auditoriaindustriales.bakend.auditorias.domain.EstadoAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuditoriaJpaRepository extends JpaRepository<Auditoria, UUID>, AuditoriaRepository {

    List<Auditoria> findAllByOrderByFechaInicioDesc();

    Optional<Auditoria> findFirstByEmpresaIdAndEstadoOrderByFechaInicioDesc(UUID empresaId, EstadoAuditoria estado);

    @Override
    default Optional<Auditoria> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Auditoria> listar() {
        return findAllByOrderByFechaInicioDesc();
    }

    @Override
    default Auditoria guardar(Auditoria auditoria) {
        return save(auditoria);
    }

    @Override
    default Optional<Auditoria> buscarEnProgresoPorEmpresa(UUID empresaId) {
        return findFirstByEmpresaIdAndEstadoOrderByFechaInicioDesc(empresaId, EstadoAuditoria.EN_PROGRESO);
    }
}
