package com.auditoriaindustriales.bakend.auditorias.infrastructure;

import com.auditoriaindustriales.bakend.auditorias.domain.Respuesta;
import com.auditoriaindustriales.bakend.auditorias.domain.RespuestaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RespuestaJpaRepository extends JpaRepository<Respuesta, UUID>, RespuestaRepository {

    Optional<Respuesta> findByAuditoriaCuestionarioIdAndPreguntaId(UUID auditoriaCuestionarioId, UUID preguntaId);

    List<Respuesta> findByAuditoriaCuestionarioId(UUID auditoriaCuestionarioId);

    @Override
    default Optional<Respuesta> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default Optional<Respuesta> buscarPorAuditoriaCuestionarioYPregunta(UUID auditoriaCuestionarioId, UUID preguntaId) {
        return findByAuditoriaCuestionarioIdAndPreguntaId(auditoriaCuestionarioId, preguntaId);
    }

    @Override
    default List<Respuesta> listarPorAuditoriaCuestionario(UUID auditoriaCuestionarioId) {
        return findByAuditoriaCuestionarioId(auditoriaCuestionarioId);
    }

    @Override
    default Respuesta guardar(Respuesta respuesta) {
        return save(respuesta);
    }
}
