package com.auditoriaindustriales.bakend.catalogo.application;

import com.auditoriaindustriales.bakend.catalogo.api.PreguntaMapper;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaActualizarRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaCrearRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaResponse;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaTextoRequest;
import com.auditoriaindustriales.bakend.catalogo.domain.CuestionarioRepository;
import com.auditoriaindustriales.bakend.catalogo.domain.Pregunta;
import com.auditoriaindustriales.bakend.catalogo.domain.PreguntaRepository;
import com.auditoriaindustriales.bakend.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PreguntaService {

    private final PreguntaRepository preguntaRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final PreguntaMapper preguntaMapper;

    public PreguntaService(PreguntaRepository preguntaRepository, CuestionarioRepository cuestionarioRepository, PreguntaMapper preguntaMapper) {
        this.preguntaRepository = preguntaRepository;
        this.cuestionarioRepository = cuestionarioRepository;
        this.preguntaMapper = preguntaMapper;
    }

    @Transactional(readOnly = true)
    public List<PreguntaResponse> listarPorCuestionario(UUID cuestionarioId, boolean soloActivas) {
        return preguntaRepository.listarPorCuestionario(cuestionarioId, soloActivas).stream()
                .map(preguntaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PreguntaResponse obtener(UUID id) {
        return preguntaMapper.toResponse(buscarOFallar(id));
    }

    public PreguntaResponse crear(UUID cuestionarioId, PreguntaCrearRequest request) {
        cuestionarioRepository.buscarPorId(cuestionarioId).orElseThrow(() -> NotFoundException.of("Cuestionario", cuestionarioId));
        Pregunta pregunta = Pregunta.crear(cuestionarioId, request.numero(), request.texto(), request.evidencia(), request.seccion());
        // El trigger fn_sync_num_preguntas recalcula cuestionario.num_preguntas al hacer flush del INSERT.
        return preguntaMapper.toResponse(preguntaRepository.guardar(pregunta));
    }

    /** Cambia numero/evidencia/seccion; nunca toca texto, así que siempre está permitido sin importar el historial. */
    public PreguntaResponse actualizarSinTexto(UUID id, PreguntaActualizarRequest request) {
        Pregunta pregunta = buscarOFallar(id);
        pregunta.actualizarSinTexto(request.numero(), request.evidencia(), request.seccion());
        return preguntaMapper.toResponse(preguntaRepository.guardar(pregunta));
    }

    /**
     * Intento de UPDATE directo del enunciado. Si la pregunta ya tiene
     * respuestas, el trigger de Postgres lo rechaza y GlobalExceptionHandler
     * lo traduce a 409 — en ese caso el frontend debe llamar a
     * reemplazarConHistorial en su lugar.
     */
    public PreguntaResponse editarTexto(UUID id, PreguntaTextoRequest request) {
        Pregunta pregunta = buscarOFallar(id);
        pregunta.editarTexto(request.texto());
        if (request.evidencia() != null) {
            pregunta.actualizarSinTexto(pregunta.getNumero(), request.evidencia(), pregunta.getSeccion());
        }
        return preguntaMapper.toResponse(preguntaRepository.guardar(pregunta));
    }

    /**
     * Flujo correcto para una pregunta con historial: desactiva la actual y
     * crea una nueva con el mismo número y el texto corregido. Requiere
     * flush inmediato de la desactivación antes del INSERT — ver el
     * comentario de guardarInmediato en PreguntaRepository.
     */
    public PreguntaResponse reemplazarConHistorial(UUID id, PreguntaTextoRequest request) {
        Pregunta actual = buscarOFallar(id);
        actual.desactivar();
        preguntaRepository.guardarInmediato(actual);

        Pregunta nueva = Pregunta.crear(actual.getCuestionarioId(), actual.getNumero(), request.texto(), request.evidencia(), actual.getSeccion());
        return preguntaMapper.toResponse(preguntaRepository.guardar(nueva));
    }

    public void desactivar(UUID id) {
        Pregunta pregunta = buscarOFallar(id);
        pregunta.desactivar();
        preguntaRepository.guardar(pregunta);
    }

    private Pregunta buscarOFallar(UUID id) {
        return preguntaRepository.buscarPorId(id).orElseThrow(() -> NotFoundException.of("Pregunta", id));
    }
}
