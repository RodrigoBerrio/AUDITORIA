package com.auditoriaindustriales.bakend.catalogo.infrastructure;

import com.auditoriaindustriales.bakend.catalogo.domain.Pregunta;
import com.auditoriaindustriales.bakend.catalogo.domain.PreguntaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreguntaJpaRepository extends JpaRepository<Pregunta, UUID>, PreguntaRepository {

    List<Pregunta> findByCuestionarioIdAndActivoTrueOrderByNumero(UUID cuestionarioId);

    List<Pregunta> findByCuestionarioIdOrderByNumero(UUID cuestionarioId);

    @Override
    default Optional<Pregunta> buscarPorId(UUID id) {
        return findById(id);
    }

    @Override
    default List<Pregunta> listarPorCuestionario(UUID cuestionarioId, boolean soloActivas) {
        return soloActivas
                ? findByCuestionarioIdAndActivoTrueOrderByNumero(cuestionarioId)
                : findByCuestionarioIdOrderByNumero(cuestionarioId);
    }

    @Override
    default Pregunta guardar(Pregunta pregunta) {
        return save(pregunta);
    }

    @Override
    default Pregunta guardarInmediato(Pregunta pregunta) {
        return saveAndFlush(pregunta);
    }
}
