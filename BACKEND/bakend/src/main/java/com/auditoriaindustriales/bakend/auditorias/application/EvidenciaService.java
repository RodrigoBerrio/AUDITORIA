package com.auditoriaindustriales.bakend.auditorias.application;

import com.auditoriaindustriales.bakend.auditorias.api.EvidenciaMapper;
import com.auditoriaindustriales.bakend.auditorias.api.dto.EvidenciaResponse;
import com.auditoriaindustriales.bakend.auditorias.api.dto.EvidenciaSubidaRequest;
import com.auditoriaindustriales.bakend.auditorias.domain.Auditoria;
import com.auditoriaindustriales.bakend.auditorias.domain.EvidenciaFotografica;
import com.auditoriaindustriales.bakend.auditorias.domain.EvidenciaFotograficaRepository;
import com.auditoriaindustriales.bakend.shared.domain.ArchivoInvalidoException;
import com.auditoriaindustriales.bakend.shared.infrastructure.SupabaseStorageClient;
import com.auditoriaindustriales.bakend.shared.security.AutenticacionUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class EvidenciaService {

    private static final Logger log = LoggerFactory.getLogger(EvidenciaService.class);

    private final EvidenciaFotograficaRepository evidenciaRepository;
    private final AuditoriaService auditoriaService;
    private final EvidenciaMapper evidenciaMapper;
    private final SupabaseStorageClient storageClient;
    private final String bucket;
    private final long maxTamanoBytes;
    private final Set<String> tiposPermitidos;

    public EvidenciaService(
            EvidenciaFotograficaRepository evidenciaRepository,
            AuditoriaService auditoriaService,
            EvidenciaMapper evidenciaMapper,
            SupabaseStorageClient storageClient,
            @Value("${app.supabase.storage.bucket-evidencia}") String bucket,
            @Value("${app.supabase.evidencia.max-file-size-mb}") long maxTamanoMb,
            @Value("${app.supabase.evidencia.tipos-permitidos}") List<String> tiposPermitidos) {
        this.evidenciaRepository = evidenciaRepository;
        this.auditoriaService = auditoriaService;
        this.evidenciaMapper = evidenciaMapper;
        this.storageClient = storageClient;
        this.bucket = bucket;
        this.maxTamanoBytes = maxTamanoMb * 1024 * 1024;
        this.tiposPermitidos = Set.copyOf(tiposPermitidos);
    }

    @Transactional(readOnly = true)
    public List<EvidenciaResponse> listarPorAuditoria(UUID auditoriaId) {
        return evidenciaRepository.listarPorAuditoria(auditoriaId).stream().map(evidenciaMapper::toResponse).toList();
    }

    /**
     * Idempotente por clienteUuid: un reintento del mismo archivo tras un
     * fallo de red (captura offline-first) responde con la fila ya creada,
     * nunca duplica. El fast-path revisa antes de subir nada; el catch de
     * DataIntegrityViolationException cubre la carrera rara de dos
     * reintentos concurrentes llegando a la vez.
     */
    public EvidenciaResponse subir(UUID auditoriaId, MultipartFile archivo, EvidenciaSubidaRequest metadata, AutenticacionUsuario usuario) {
        var existente = evidenciaRepository.buscarPorClienteUuid(metadata.clienteUuid());
        if (existente.isPresent()) {
            log.info("evidencia clienteUuid={} ya existía (reintento idempotente) de usuario={} en auditoria={}",
                    metadata.clienteUuid(), usuario.usuarioId(), auditoriaId);
            return evidenciaMapper.toResponse(existente.get());
        }

        Auditoria auditoria = auditoriaService.buscarOFallar(auditoriaId);
        auditoria.exigirPermisoEscritura(usuario);
        validarArchivo(archivo);

        String path = "%s/%s%s".formatted(auditoriaId, metadata.clienteUuid(), extensionPara(archivo.getContentType()));
        String url = subirArchivo(archivo, path);

        EvidenciaFotografica evidencia = EvidenciaFotografica.registrar(
                auditoriaId,
                metadata.respuestaId(),
                url,
                metadata.descripcion(),
                usuario.usuarioId(),
                metadata.clienteUuid(),
                metadata.tomadaEn() != null ? metadata.tomadaEn() : Instant.now());

        try {
            EvidenciaFotografica guardada = evidenciaRepository.guardar(evidencia);
            log.info("evidencia={} subida por usuario={} en auditoria={}", guardada.getId(), usuario.usuarioId(), auditoriaId);
            return evidenciaMapper.toResponse(guardada);
        } catch (DataIntegrityViolationException ex) {
            return evidenciaMapper.toResponse(evidenciaRepository.buscarPorClienteUuid(metadata.clienteUuid()).orElseThrow(() -> ex));
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new ArchivoInvalidoException("El archivo de evidencia está vacío.");
        }
        if (archivo.getSize() > maxTamanoBytes) {
            throw new ArchivoInvalidoException("El archivo supera el tamaño máximo permitido de %d MB.".formatted(maxTamanoBytes / (1024 * 1024)));
        }
        String contentType = archivo.getContentType();
        if (contentType == null || !tiposPermitidos.contains(contentType)) {
            throw new ArchivoInvalidoException("Tipo de archivo no permitido: %s. Tipos permitidos: %s.".formatted(contentType, tiposPermitidos));
        }
    }

    private String subirArchivo(MultipartFile archivo, String path) {
        try {
            return storageClient.subir(bucket, path, archivo.getBytes(), archivo.getContentType());
        } catch (IOException ex) {
            throw new UncheckedIOException("No se pudo leer el archivo de evidencia recibido.", ex);
        }
    }

    private String extensionPara(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/heic" -> ".heic";
            default -> ".jpg";
        };
    }
}
