package com.auditoriaindustriales.bakend.resultados.api;

import com.auditoriaindustriales.bakend.resultados.api.dto.HallazgosResumenResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.RankingResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.ResumenAuditoriaResponse;
import com.auditoriaindustriales.bakend.resultados.api.dto.SeccionesResponse;
import com.auditoriaindustriales.bakend.resultados.application.ResultadosService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class ResultadosController {

    private final ResultadosService resultadosService;

    public ResultadosController(ResultadosService resultadosService) {
        this.resultadosService = resultadosService;
    }

    @GetMapping("/api/auditorias/{auditoriaId}/resumen")
    public ResumenAuditoriaResponse resumen(@PathVariable UUID auditoriaId, @RequestParam(required = false) BigDecimal meta) {
        return resultadosService.resumen(auditoriaId, meta);
    }

    @GetMapping("/api/auditorias/{auditoriaId}/ranking")
    public RankingResponse ranking(@PathVariable UUID auditoriaId) {
        return resultadosService.ranking(auditoriaId);
    }

    @GetMapping("/api/auditorias/{auditoriaId}/cuestionarios/{cuestionarioId}/secciones")
    public SeccionesResponse secciones(@PathVariable UUID auditoriaId, @PathVariable UUID cuestionarioId) {
        return resultadosService.secciones(auditoriaId, cuestionarioId);
    }

    @GetMapping("/api/auditorias/{auditoriaId}/hallazgos/resumen")
    public HallazgosResumenResponse hallazgosResumen(@PathVariable UUID auditoriaId) {
        return resultadosService.hallazgosResumen(auditoriaId);
    }
}
