-- ============================================================
--  Migración v4 — Evidencia fotográfica de visitas
--  No reutiliza pregunta.evidencia (texto descriptivo, no
--  almacenamiento de archivos). El archivo real vive en
--  Supabase Storage; aquí solo se guarda la referencia.
--
--  cliente_uuid es la clave de idempotencia: se genera en el
--  frontend antes de subir la foto (captura offline-first vía
--  IndexedDB), para que un reintento tras fallo de red no cree
--  una fila duplicada.
-- ============================================================

CREATE TABLE evidencia_fotografica (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditoria_id    UUID NOT NULL REFERENCES auditoria(id) ON DELETE CASCADE,
    respuesta_id    UUID REFERENCES respuesta(id) ON DELETE SET NULL,
    url_archivo     VARCHAR(500) NOT NULL,
    descripcion     VARCHAR(255),
    subida_por      UUID NOT NULL REFERENCES usuario(id) ON DELETE RESTRICT,
    cliente_uuid    UUID NOT NULL UNIQUE,
    tomada_en       TIMESTAMP NOT NULL DEFAULT now(),
    subida_en       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_evidencia_auditoria ON evidencia_fotografica(auditoria_id);
CREATE INDEX idx_evidencia_respuesta ON evidencia_fotografica(respuesta_id);
