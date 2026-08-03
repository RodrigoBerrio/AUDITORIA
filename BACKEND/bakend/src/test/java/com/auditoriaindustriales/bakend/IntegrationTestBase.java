package com.auditoriaindustriales.bakend;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Stream;

/**
 * Levanta un Postgres real en Testcontainers y le aplica las migraciones SQL
 * versionadas del proyecto (las mismas que corren en Supabase), para probar
 * contra el esquema real —triggers, checks e índices incluidos— en vez de un
 * esquema autogenerado por Hibernate. Las migraciones viven en
 * FRONTEND/supabase/migrations (fuera de este módulo Maven); la ruta relativa
 * asume que los tests corren con working directory = BACKEND/bakend.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    private static final Path CARPETA_MIGRACIONES = Path.of("../../FRONTEND/supabase/migrations");

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @BeforeAll
    static void aplicarMigraciones() throws Exception {
        try (Connection conexion = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement sentencia = conexion.createStatement();
             Stream<Path> archivos = Files.list(CARPETA_MIGRACIONES)) {
            archivos.filter(p -> p.toString().endsWith(".sql"))
                    .sorted()
                    .forEach(archivo -> ejecutar(sentencia, archivo));
        }
    }

    private static void ejecutar(Statement sentencia, Path archivo) {
        try {
            // Todo el archivo en un solo execute: los cuerpos de función con $$...$$
            // contienen ";" internos que una división ingenua por líneas rompería.
            sentencia.execute(Files.readString(archivo));
        } catch (Exception ex) {
            throw new IllegalStateException("Error aplicando la migración " + archivo, ex);
        }
    }
}
