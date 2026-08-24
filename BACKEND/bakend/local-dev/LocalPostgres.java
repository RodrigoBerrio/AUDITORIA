import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Postgres local embebido para desarrollo/demo sin Docker (que no arranca en
 * este entorno) ni Supabase (proyecto real pausado por inactividad — ver
 * memoria "supabase-proyecto-real"). Detecta solo si pgdata/ ya está
 * inicializado (existe PG_VERSION): si es la primera vez, inicializa desde
 * cero; si ya existe, reutiliza los datos tal cual.
 *
 * En AMBOS casos compara los archivos de FRONTEND/supabase/migrations contra
 * una tabla de rastreo (_local_dev_migraciones_aplicadas, vive dentro de
 * pgdata/ y por lo tanto viaja con cualquier copia/snapshot de la carpeta) y
 * aplica las que falten. Esto hace que el arranque se autocorrija solo si
 * pgdata/ termina siendo una copia más vieja que la última usada (este
 * sandbox no garantiza persistir esa carpeta de forma confiable entre
 * sesiones — ya pasó dos veces, 2026-08-19 y 2026-08-24 — y antes esto se
 * descubría recién cuando el backend fallaba al arrancar o el usuario notaba
 * datos viejos).
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

        sincronizarMigraciones(pg, yaInicializado);

        Thread.currentThread().join();
    }

    /**
     * yaInicializado=false (pgdata nuevo): la tabla de rastreo tampoco existe, así que todas
     * las migraciones se aplican y se registran normalmente.
     * yaInicializado=true y la tabla de rastreo NO existe (primera vez que corre esta versión
     * de la herramienta contra un pgdata más viejo, o pgdata reciclado a mano): se asume que el
     * esquema actual ya refleja todas las migraciones presentes hoy y solo se registran sin
     * ejecutarlas — evita reventar con "column already exists" al reintroducir el rastreo.
     * yaInicializado=true y la tabla de rastreo SÍ existe (caso normal de aquí en adelante):
     * solo se aplican y registran los archivos que falten — este es el caso que se autocorrige
     * si pgdata resultó ser una copia vieja.
     */
    private static void sincronizarMigraciones(EmbeddedPostgres pg, boolean yaInicializado) throws Exception {
        Path migraciones = Path.of("../../../FRONTEND/supabase/migrations").toAbsolutePath().normalize();
        try (Connection conexion = pg.getPostgresDatabase().getConnection()) {
            boolean rastreoExistia = existeTabla(conexion, "_local_dev_migraciones_aplicadas");
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute("CREATE TABLE IF NOT EXISTS _local_dev_migraciones_aplicadas "
                        + "(nombre_archivo TEXT PRIMARY KEY, aplicado_en TIMESTAMPTZ NOT NULL DEFAULT now())");
            }

            Set<String> yaRegistradas = new HashSet<>();
            try (Statement sentencia = conexion.createStatement();
                 ResultSet rs = sentencia.executeQuery("SELECT nombre_archivo FROM _local_dev_migraciones_aplicadas")) {
                while (rs.next()) yaRegistradas.add(rs.getString(1));
            }

            boolean soloRegistrarSinEjecutar = yaInicializado && !rastreoExistia;
            if (soloRegistrarSinEjecutar) {
                System.out.println("Rastreo de migraciones nuevo sobre pgdata existente: se asume el esquema actual al dia, solo se registra.");
            }

            try (PreparedStatement registrar = conexion.prepareStatement(
                    "INSERT INTO _local_dev_migraciones_aplicadas (nombre_archivo) VALUES (?)");
                 Statement sentencia = conexion.createStatement();
                 Stream<Path> archivos = Files.list(migraciones)) {
                List<Path> pendientes = archivos.filter(p -> p.toString().endsWith(".sql"))
                        .sorted(Comparator.naturalOrder())
                        .filter(p -> !yaRegistradas.contains(p.getFileName().toString()))
                        .toList();

                for (Path archivo : pendientes) {
                    if (!soloRegistrarSinEjecutar) {
                        System.out.println("Aplicando " + archivo.getFileName());
                        sentencia.execute(Files.readString(archivo));
                    }
                    registrar.setString(1, archivo.getFileName().toString());
                    registrar.executeUpdate();
                }

                if (!soloRegistrarSinEjecutar && !pendientes.isEmpty()) {
                    System.out.println("MIGRATIONS_APPLIED (" + pendientes.size() + ")");
                }
            }
        }
    }

    private static boolean existeTabla(Connection conexion, String nombre) throws Exception {
        try (ResultSet rs = conexion.getMetaData().getTables(null, "public", nombre, null)) {
            return rs.next();
        }
    }
}
