package com.auditoriaindustriales.bakend.catalogo.api;

import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaActualizarRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaCrearRequest;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaResponse;
import com.auditoriaindustriales.bakend.catalogo.api.dto.PreguntaTextoRequest;
import com.auditoriaindustriales.bakend.catalogo.application.PreguntaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class PreguntaController {

    private final PreguntaService preguntaService;

    public PreguntaController(PreguntaService preguntaService) {
        this.preguntaService = preguntaService;
    }

    @GetMapping("/api/cuestionarios/{cuestionarioId}/preguntas")
    public List<PreguntaResponse> listar(@PathVariable UUID cuestionarioId, @RequestParam(defaultValue = "true") boolean soloActivas) {
        return preguntaService.listarPorCuestionario(cuestionarioId, soloActivas);
    }

    @PostMapping("/api/cuestionarios/{cuestionarioId}/preguntas")
    @ResponseStatus(HttpStatus.CREATED)
    public PreguntaResponse crear(@PathVariable UUID cuestionarioId, @Valid @RequestBody PreguntaCrearRequest request) {
        return preguntaService.crear(cuestionarioId, request);
    }

    @GetMapping("/api/preguntas/{id}")
    public PreguntaResponse obtener(@PathVariable UUID id) {
        return preguntaService.obtener(id);
    }

    @PutMapping("/api/preguntas/{id}")
    public PreguntaResponse actualizarSinTexto(@PathVariable UUID id, @Valid @RequestBody PreguntaActualizarRequest request) {
        return preguntaService.actualizarSinTexto(id, request);
    }

    /** Puede responder 409 si la pregunta ya tiene respuestas — usar /reemplazar en ese caso. */
    @PutMapping("/api/preguntas/{id}/texto")
    public PreguntaResponse editarTexto(@PathVariable UUID id, @Valid @RequestBody PreguntaTextoRequest request) {
        return preguntaService.editarTexto(id, request);
    }

    @PostMapping("/api/preguntas/{id}/reemplazar")
    public PreguntaResponse reemplazarConHistorial(@PathVariable UUID id, @Valid @RequestBody PreguntaTextoRequest request) {
        return preguntaService.reemplazarConHistorial(id, request);
    }

    @DeleteMapping("/api/preguntas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        preguntaService.desactivar(id);
    }
}
