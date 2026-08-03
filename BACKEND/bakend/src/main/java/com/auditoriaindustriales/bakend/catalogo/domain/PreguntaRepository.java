package com.auditoriaindustriales.bakend.catalogo.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PreguntaRepository {

    Optional<Pregunta> buscarPorId(UUID id);

    List<Pregunta> listarPorCuestionario(UUID cuestionarioId, boolean soloActivas);

    Pregunta guardar(Pregunta pregunta);

    /**
     * Guarda forzando un flush inmediato. Necesario en reemplazarConHistorial:
     * Hibernate ejecuta todos los INSERT de un flush antes que los UPDATE
     * (sin importar el orden en que se llamó a save()), así que sin este
     * flush explícito la pregunta nueva se insertaría mientras la antigua
     * todavía figura activa=true, violando el índice único parcial
     * uq_pregunta_cuestionario_numero_activa.
     */
    Pregunta guardarInmediato(Pregunta pregunta);
}
