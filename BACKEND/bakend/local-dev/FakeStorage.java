import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Stub mínimo de la REST API de Supabase Storage, solo para desarrollo/demo
 * local sin Supabase real (proyecto pausado — ver memoria "supabase-proyecto-real").
 * Apunta SUPABASE_STORAGE_URL=http://localhost:9999/storage/v1 en .env.
 * Sin dependencias externas (usa com.sun.net.httpserver, viene con el JDK).
 *
 * Uso: javac FakeStorage.java && java FakeStorage
 */
public class FakeStorage {
    static final Path RAIZ = Path.of(".").resolve("storage").toAbsolutePath().normalize();

    public static void main(String[] args) throws Exception {
        Files.createDirectories(RAIZ);
        HttpServer server = HttpServer.create(new InetSocketAddress(9999), 0);
        server.createContext("/storage/v1/object/", new Handler());
        server.setExecutor(null);
        server.start();
        System.out.println("FAKE_STORAGE_READY puerto=9999 raiz=" + RAIZ);
        Thread.currentThread().join();
    }

    static class Handler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws java.io.IOException {
            String path = exchange.getRequestURI().getPath().substring("/storage/v1/object/".length());
            boolean esPublico = path.startsWith("public/");
            if (esPublico) path = path.substring("public/".length());
            File archivo = RAIZ.resolve(path).toFile();

            String metodo = exchange.getRequestMethod();
            if (metodo.equals("POST") || metodo.equals("PUT")) {
                archivo.getParentFile().mkdirs();
                try (var in = exchange.getRequestBody(); var out = new FileOutputStream(archivo)) {
                    in.transferTo(out);
                }
                byte[] resp = "{\"Key\":\"ok\"}".getBytes();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, resp.length);
                exchange.getResponseBody().write(resp);
                exchange.close();
            } else if (metodo.equals("GET")) {
                if (!archivo.exists()) {
                    exchange.sendResponseHeaders(404, -1);
                    exchange.close();
                    return;
                }
                exchange.getResponseHeaders().set("Content-Type", "application/pdf");
                exchange.sendResponseHeaders(200, archivo.length());
                try (var in = new FileInputStream(archivo); var out = exchange.getResponseBody()) {
                    in.transferTo(out);
                }
                exchange.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }
}
