package com.auditoriaindustriales.bakend.auditorias.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * clienteUuid es la clave de idempotencia: se genera en el frontend antes de
 * subir la foto (captura offline-first). Ver EvidenciaService.subir — nunca
 * se debe crear una segunda fila para el mismo clienteUuid.
 */
@Entity
@Table(name = "evidencia_fotografica")
public class EvidenciaFotografica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "auditoria_id", nullable = false)
    private UUID auditoriaId;

    @Column(name = "respuesta_id")
    private UUID respuestaId;

    @Column(name = "url_archivo", nullable = false, length = 500)
    private String urlArchivo;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "subida_por", nullable = false)
    private UUID subidaPor;

    @Column(name = "cliente_uuid", nullable = false, unique = true)
    private UUID clienteUuid;

    @Column(name = "tomada_en", nullable = false)
    private Instant tomadaEn;

    @Column(name = "subida_en", insertable = false, updatable = false)
    private Instant subidaEn;

    protected EvidenciaFotografica() {
        // JPA
    }

    private EvidenciaFotografica(
            UUID auditoriaId, UUID respuestaId, String urlArchivo, String descripcion,
            UUID subidaPor, UUID clienteUuid, Instant tomadaEn) {
        this.auditoriaId = auditoriaId;
        this.respuestaId = respuestaId;
        this.urlArchivo = urlArchivo;
        this.descripcion = descripcion;
        this.subidaPor = subidaPor;
        this.clienteUuid = clienteUuid;
        this.tomadaEn = tomadaEn;
    }

    public static EvidenciaFotografica registrar(
            UUID auditoriaId, UUID respuestaId, String urlArchivo, String descripcion,
            UUID subidaPor, UUID clienteUuid, Instant tomadaEn) {
        return new EvidenciaFotografica(auditoriaId, respuestaId, urlArchivo, descripcion, subidaPor, clienteUuid, tomadaEn);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAuditoriaId() {
        return auditoriaId;
    }

    public UUID getRespuestaId() {
        return respuestaId;
    }

    public String getUrlArchivo() {
        return urlArchivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public UUID getSubidaPor() {
        return subidaPor;
    }

    public UUID getClienteUuid() {
        return clienteUuid;
    }

    public Instant getTomadaEn() {
        return tomadaEn;
    }

    public Instant getSubidaEn() {
        return subidaEn;
    }
}
