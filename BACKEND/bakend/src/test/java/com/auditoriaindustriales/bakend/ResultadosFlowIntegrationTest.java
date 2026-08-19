package com.auditoriaindustriales.bakend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Siembra dos cuestionarios (con secciones dentro de uno de ellos) y dos
 * hallazgos con valores conocidos, y verifica que los endpoints de
 * resultados agregados (resumen, ranking, secciones, hallazgos/resumen,
 * empresas/historico) calculan exactamente lo esperado — mismos números que
 * consumirán el frontend y el PDF, sobre Postgres real (triggers incluidos).
 */
class ResultadosFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String accessToken;

    @BeforeEach
    void seedUsuarioYLogin() throws Exception {
        String hash = new BCryptPasswordEncoder().encode("clave1234");
        try (Connection conexion = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    INSERT INTO usuario (nombre, correo, password_hash, rol)
                    VALUES ('Auditor de Prueba', 'auditor2@test.com', '%s', 'auditor')
                    ON CONFLICT (correo) DO NOTHING
                    """.formatted(hash));
        }

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"auditor2@test.com","password":"clave1234"}"""))
                .andExpect(status().isOk())
                .andReturn();
        accessToken = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void resumenRankingSeccionesYHallazgosCalculanLosValoresEsperados() throws Exception {
        String empresaId = crear("/api/empresas", """
                {"razonSocial":"Empresa Resultados","nit":"900555666-1"}""").get("id").asText();

        String categoriaId = crear("/api/categorias", """
                {"nombre":"Mantenimiento Resultados","esPlantilla":true}""").get("id").asText();

        String subcatDiagnostico = crear("/api/categorias/%s/subcategorias".formatted(categoriaId), """
                {"nombre":"Diagnóstico energético","esPlantilla":true}""").get("id").asText();
        String subcatCultura = crear("/api/categorias/%s/subcategorias".formatted(categoriaId), """
                {"nombre":"Cultura de mantenimiento","esPlantilla":true}""").get("id").asText();

        String cuestionarioA = crear("/api/subcategorias/%s/cuestionarios".formatted(subcatDiagnostico), """
                {"nombre":"Cuestionario Diagnóstico"}""").get("id").asText();
        String cuestionarioB = crear("/api/subcategorias/%s/cuestionarios".formatted(subcatCultura), """
                {"nombre":"Cuestionario Cultura"}""").get("id").asText();

        String preguntaA1 = crear("/api/cuestionarios/%s/preguntas".formatted(cuestionarioA), """
                {"numero":1,"texto":"¿Pregunta A1?","seccion":"Sección 1"}""").get("id").asText();
        String preguntaA2 = crear("/api/cuestionarios/%s/preguntas".formatted(cuestionarioA), """
                {"numero":2,"texto":"¿Pregunta A2?","seccion":"Sección 2"}""").get("id").asText();
        String preguntaB1 = crear("/api/cuestionarios/%s/preguntas".formatted(cuestionarioB), """
                {"numero":1,"texto":"¿Pregunta B1?"}""").get("id").asText();

        String auditoriaId = crear("/api/auditorias", """
                {"empresaId":"%s"}""".formatted(empresaId)).get("id").asText();

        String acA = crear("/api/auditorias/%s/cuestionarios".formatted(auditoriaId), """
                {"cuestionarioId":"%s"}""".formatted(cuestionarioA)).get("id").asText();
        String acB = crear("/api/auditorias/%s/cuestionarios".formatted(auditoriaId), """
                {"cuestionarioId":"%s"}""".formatted(cuestionarioB)).get("id").asText();

        responder(acA, preguntaA1, 5);
        responder(acA, preguntaA2, 3);
        responder(acB, preguntaB1, 2);

        crear("/api/auditorias/%s/hallazgos".formatted(auditoriaId), """
                {"descripcion":"Hallazgo crítico","severidad":"critica"}""");
        crear("/api/auditorias/%s/hallazgos".formatted(auditoriaId), """
                {"descripcion":"Hallazgo medio","severidad":"media"}""");
        JsonNode hallazgoCerrado = crear("/api/auditorias/%s/hallazgos".formatted(auditoriaId), """
                {"descripcion":"Hallazgo a cerrar","severidad":"baja"}""");
        mockMvc.perform(patch("/api/hallazgos/%s/estado".formatted(hallazgoCerrado.get("id").asText()))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"estado":"cerrado"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auditorias/%s/finalizar".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntajeGlobal").value(3.0));

        // Cuestionario A: avg(5,3) = 4.00 (CONSOLIDADO); Cuestionario B: 2.00 (CRITICO); global = avg(4,2) = 3.00 (EN_DESARROLLO)
        mockMvc.perform(get("/api/auditorias/%s/resumen".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntajeGlobal").value(3.0))
                .andExpect(jsonPath("$.nivelMadurez").value("Preventivo"))
                .andExpect(jsonPath("$.colorSemaforo").value("#D4860A"))
                .andExpect(jsonPath("$.subcategorias.length()").value(2))
                .andExpect(jsonPath("$.subcategorias[?(@.subcategoria=='Diagnóstico energético')].puntaje").value(4.0))
                .andExpect(jsonPath("$.subcategorias[?(@.subcategoria=='Cultura de mantenimiento')].colorSemaforo").value("#C0392B"))
                .andExpect(jsonPath("$.preguntasRespondidas").value(3))
                .andExpect(jsonPath("$.preguntasEsperadas").value(3));

        // Ranking ascendente: Cultura (2.00, crítico) primero, Diagnóstico (4.00, consolidado) después
        mockMvc.perform(get("/api/auditorias/%s/ranking".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].etiqueta").value("Cultura de mantenimiento"))
                .andExpect(jsonPath("$.items[0].puntaje").value(2.0))
                .andExpect(jsonPath("$.items[0].colorSemaforo").value("#C0392B"))
                .andExpect(jsonPath("$.items[1].etiqueta").value("Diagnóstico energético"))
                .andExpect(jsonPath("$.items[1].puntaje").value(4.0));

        // Secciones del cuestionario A: Sección 2 (3.00) antes que Sección 1 (5.00), ranking ascendente
        mockMvc.perform(get("/api/auditorias/%s/cuestionarios/%s/secciones".formatted(auditoriaId, cuestionarioA))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].etiqueta").value("Sección 2"))
                .andExpect(jsonPath("$.items[0].puntaje").value(3.0))
                .andExpect(jsonPath("$.items[1].etiqueta").value("Sección 1"))
                .andExpect(jsonPath("$.items[1].puntaje").value(5.0))
                .andExpect(jsonPath("$.preguntasRespondidas").value(2))
                .andExpect(jsonPath("$.preguntasEsperadas").value(2));

        // Cuestionario B: sin seccion asignada -> agrupa en "General"
        mockMvc.perform(get("/api/auditorias/%s/cuestionarios/%s/secciones".formatted(auditoriaId, cuestionarioB))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].etiqueta").value("General"))
                .andExpect(jsonPath("$.items[0].puntaje").value(2.0));

        // Hallazgos: 1 crítica, 1 media, 1 baja (33.3% cada una); por estado: 2 abiertos, 1 cerrado
        mockMvc.perform(get("/api/auditorias/%s/hallazgos/resumen".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.porSeveridad[?(@.severidad=='critica')].cantidad").value(1))
                .andExpect(jsonPath("$.porSeveridad[?(@.severidad=='critica')].colorHex").value("#C0392B"))
                .andExpect(jsonPath("$.porEstado[?(@.estado=='cerrado')].cantidad").value(1))
                .andExpect(jsonPath("$.porEstado[?(@.estado=='abierto')].cantidad").value(2));

        // Histórico de la empresa: una sola auditoría finalizada hasta ahora
        mockMvc.perform(get("/api/empresas/%s/historico".formatted(empresaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].puntajeGlobal").value(3.0));
    }

    private void responder(String auditoriaCuestionarioId, String preguntaId, int valor) throws Exception {
        mockMvc.perform(put("/api/auditoria-cuestionarios/%s/respuestas".formatted(auditoriaCuestionarioId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"preguntaId":"%s","valor":%d}""".formatted(preguntaId, valor)))
                .andExpect(status().isOk());
    }

    private JsonNode crear(String url, String body) throws Exception {
        MvcResult resultado = mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        return objectMapper.readTree(resultado.getResponse().getContentAsString());
    }
}
