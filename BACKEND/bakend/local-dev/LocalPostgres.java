import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Postgres local embebido para desarrollo/demo sin Docker (que no arranca en
 * este entorno) ni Supabase (proyecto real pausado por inactividad — ver
 * memoria "supabase-proyecto-real"). Detecta solo si pgdata/ ya está
 * inicializado (existe PG_VERSION): si es la primera vez, inicializa y aplica
 * las migraciones de FRONTEND/supabase/migrations; si ya existe, reutiliza
 * los datos tal cual sin volver a aplicar nada.
 *
 * Uso (desde este directorio, con el classpath de io.zonky.test:embedded-postgres
 * ya resuelto en ~/.m2 — ver build-classpath.txt o resolver de nuevo con Maven):
 *   javac -cp "%CP%" LocalPostgres.java
 *   java -cp ".;%CP%" LocalPostgres
 *
 * Puerto 5432, usuario/clave "postgres" — coincide con BACKEND/bakend/.env.
 */
public class LocalPostgres {
    public static void main(String[] args) throws Exception {
        Path dataDir = Path.of(".").resolve("pgdata").toAbsolutePath().normalize();
        boolean yaInicializado = Files.exists(dataDir.resolve("PG_VERSION"));

        EmbeddedPostgres pg = EmbeddedPostgres.builder()
                .setPort(5432)
                .setDataDirectory(dataDir)
                .setCleanDataDirectory(!yaInicializado)
                .start();

        System.out.println("PG_READY jdbcUrl=" + pg.getJdbcUrl("postgres", "postgres") + " (yaInicializado=" + yaInicializado + ")");

        if (!yaInicializado) {
            aplicarMigraciones(pg);
        }

        Thread.currentThread().join();
    }

    private static void aplicarMigraciones(EmbeddedPostgres pg) throws Exception {
        Path migraciones = Path.of("../../../FRONTEND/supabase/migrations").toAbsolutePath().normalize();
        try (Connection conexion = pg.getPostgresDatabase().getConnection();
             Statement sentencia = conexion.createStatement();
             Stream<Path> archivos = Files.list(migraciones)) {
            archivos.filter(p -> p.toString().endsWith(".sql"))
                    .sorted(Comparator.naturalOrder())
                    .forEach(archivo -> {
                        try {
                            System.out.println("Aplicando " + archivo.getFileName());
                            sentencia.execute(Files.readString(archivo));
                        } catch (Exception ex) {
                            throw new RuntimeException("Error aplicando " + archivo, ex);
                        }
                    });
        }
        System.out.println("MIGRATIONS_APPLIED");
    }
}
