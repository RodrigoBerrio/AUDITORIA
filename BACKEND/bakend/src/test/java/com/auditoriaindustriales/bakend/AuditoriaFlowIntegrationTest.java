package com.auditoriaindustriales.bakend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.auditoriaindustriales.bakend.shared.infrastructure.SupabaseStorageClient;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Recorre el flujo real del auditor de punta a punta contra Postgres real
 * (Testcontainers + migraciones reales): login, empresa, catálogo, auditoría,
 * respuesta (dispara los triggers de puntaje), protección de pregunta.texto,
 * y generación de reporte. No es una prueba unitaria de una capa aislada:
 * verifica que el esquema real, los triggers y el código Java concuerdan.
 */
class AuditoriaFlowIntegrationTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    // Instancia propia: Spring Boot 4 configura por defecto el JsonMapper de
    // Jackson 3 (tools.jackson.databind), no un bean com.fasterxml.jackson.databind.ObjectMapper.
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SupabaseStorageClient supabaseStorageClient;

    private String accessToken;

    @BeforeEach
    void seedUsuarioYLogin() throws Exception {
        insertarUsuarioAuditor();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"correo":"auditor@test.com","password":"clave1234"}"""))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();

        when(supabaseStorageClient.subir(anyString(), anyString(), any(), anyString()))
                .thenReturn("https://fake-storage.test/object/public/reportes/fake.pdf");
    }

    private void insertarUsuarioAuditor() throws Exception {
        String hash = new BCryptPasswordEncoder().encode("clave1234");
        try (Connection conexion = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    INSERT INTO usuario (nombre, correo, password_hash, rol)
                    VALUES ('Auditor de Prueba', 'auditor@test.com', '%s', 'auditor')
                    ON CONFLICT (correo) DO NOTHING
                    """.formatted(hash));
        }
    }

    @Test
    void flujoCompletoDeAuditoria() throws Exception {
        String empresaId = crear("/api/empresas", """
                {"razonSocial":"Empresa de Prueba","nit":"900999999-1"}""").get("id").asText();

        String categoriaId = crear("/api/categorias", """
                {"nombre":"Mantenimiento","esPlantilla":true}""").get("id").asText();

        String subcategoriaId = crear("/api/categorias/%s/subcategorias".formatted(categoriaId), """
                {"nombre":"Ordenes de trabajo","esPlantilla":true}""").get("id").asText();

        String cuestionarioId = crear("/api/subcategorias/%s/cuestionarios".formatted(subcategoriaId), """
                {"nombre":"Cuestionario OT"}""").get("id").asText();

        JsonNode pregunta = crear("/api/cuestionarios/%s/preguntas".formatted(cuestionarioId), """
                {"numero":1,"texto":"¿Existe un formato de OT?","evidencia":"Formato físico"}""");
        String preguntaId = pregunta.get("id").asText();

        String auditoriaId = crear("/api/auditorias", """
                {"empresaId":"%s"}""".formatted(empresaId)).get("id").asText();

        String auditoriaCuestionarioId = crear("/api/auditorias/%s/cuestionarios".formatted(auditoriaId), """
                {"cuestionarioId":"%s"}""".formatted(cuestionarioId)).get("id").asText();

        // Responder dispara fn_sync_puntaje_cuestionario / fn_sync_puntaje_global
        mockMvc.perform(put("/api/auditoria-cuestionarios/%s/respuestas".formatted(auditoriaCuestionarioId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"preguntaId":"%s","valor":4}""".formatted(preguntaId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valor").value(4));

        // El cuestionario aplicado ya no debe estar "pendiente"
        mockMvc.perform(get("/api/auditorias/%s/cuestionarios".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("en_progreso"))
                .andExpect(jsonPath("$[0].puntaje").value(4.0));

        mockMvc.perform(post("/api/auditorias/%s/finalizar".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("finalizada"))
                .andExpect(jsonPath("$.puntajeGlobal").value(4.0));

        // La pregunta ya tiene respuesta: el trigger debe rechazar el UPDATE directo del texto (409)
        mockMvc.perform(put("/api/preguntas/%s/texto".formatted(preguntaId))
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"texto":"¿Existe un formato de OT actualizado?"}"""))
                .andExpect(status().isConflict());

        // El flujo correcto: desactivar y crear una nueva con el mismo número
        JsonNode preguntaReemplazada = objectMapper.readTree(mockMvc.perform(
                        post("/api/preguntas/%s/reemplazar".formatted(preguntaId))
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {"texto":"¿Existe un formato de OT actualizado?"}"""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        assertThat(preguntaReemplazada.get("numero").asInt()).isEqualTo(1);
        assertThat(preguntaReemplazada.get("id").asText()).isNotEqualTo(preguntaId);

        // Reporte: requiere auditoría finalizada con puntaje ya calculado
        mockMvc.perform(post("/api/auditorias/%s/reportes".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nivelMadurez").value("Predictivo"))
                .andExpect(jsonPath("$.rutaPdf").value("https://fake-storage.test/object/public/reportes/fake.pdf"));

        // El PDF ya no es solo texto: trae al menos el radar y el ranking como imágenes PNG reales.
        ArgumentCaptor<byte[]> pdfCapturado = ArgumentCaptor.forClass(byte[].class);
        org.mockito.Mockito.verify(supabaseStorageClient).subir(anyString(), anyString(), pdfCapturado.capture(), anyString());
        assertThat(pdfCapturado.getValue().length).isGreaterThan(20_000);
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/empresas")).andExpect(status().isUnauthorized());
    }

    @Test
    void transicionDeEstadoInvalidaDevuelve409() throws Exception {
        String empresaId = crear("/api/empresas", """
                {"razonSocial":"Empresa X","nit":"900111222-3"}""").get("id").asText();
        String auditoriaId = crear("/api/auditorias", """
                {"empresaId":"%s"}""".formatted(empresaId)).get("id").asText();

        mockMvc.perform(post("/api/auditorias/%s/finalizar".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Ya está finalizada: no puede volver a finalizarse ni cancelarse
        mockMvc.perform(post("/api/auditorias/%s/cancelar".formatted(auditoriaId))
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
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
