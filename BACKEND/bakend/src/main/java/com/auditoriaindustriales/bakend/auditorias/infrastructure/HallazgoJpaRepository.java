package com.auditoriaindustriales.bakend.auditorias.infrastructure;

import com.auditoriaindustriales.bakend.auditorias.domain.Hallazgo;
import com.auditoriaindustriales.bakend.auditorias.domain.HallazgoRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HallazgoJpaRepository extends JpaRepository<Hallazgo, UUID>, HallazgoRepository {

    List<Hallazgo> findByAuditoriaId(UUID auditoriaId);

    @Override
    default Optional<Hallazgo> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Hallazgo> listarPorAuditoria(UUID auditoriaId) {
        return findByAuditoriaId(auditoriaId);
    }

    @Override
    default Hallazgo guardar(Hallazgo hallazgo) {
        return save(hallazgo);
    }
}
