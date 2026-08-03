package com.auditoriaindustriales.bakend.shared.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente mínimo sobre la REST API de Supabase Storage. Vive en shared (no en
 * un módulo) porque tanto evidencia fotográfica (auditorias) como los PDF de
 * reporte (reportes) lo necesitan igual: subir un archivo y obtener su URL,
 * nada específico del dominio de ninguno de los dos.
 */
@Component
public class SupabaseStorageClient {

    private final RestClient restClient;
    private final String storageUrl;
    private final String serviceKey;

    public SupabaseStorageClient(
            @Value("${app.supabase.storage.url}") String storageUrl,
            @Value("${app.supabase.storage.service-key}") String serviceKey) {
        this.storageUrl = storageUrl;
        this.serviceKey = serviceKey;
        this.restClient = RestClient.builder().baseUrl(storageUrl).build();
    }

    /** Sube (o sobrescribe, x-upsert) el archivo y devuelve la URL pública. */
    public String subir(String bucket, String path, byte[] contenido, String contentType) {
        restClient.post()
                .uri("/object/{bucket}/{path}", bucket, path)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .header("x-upsert", "true")
                .contentType(MediaType.parseMediaType(contentType))
                .body(contenido)
                .retrieve()
                .toBodilessEntity();

        return "%s/object/public/%s/%s".formatted(storageUrl, bucket, path);
    }
}
