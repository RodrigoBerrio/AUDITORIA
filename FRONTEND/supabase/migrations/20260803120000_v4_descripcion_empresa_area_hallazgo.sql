-- ============================================================
--  Migración v4 — Auditorías Industriales SAS
--  El frontend (domain.ts) ya espera estos dos campos; no
--  existían en v3. Sin lógica adicional: texto libre.
-- ============================================================

ALTER TABLE empresa  ADD COLUMN descripcion VARCHAR(500);
ALTER TABLE hallazgo ADD COLUMN area        VARCHAR(100);

-- ============================================================
--  Corrección: UNIQUE(cuestionario_id, numero) no eximía las
--  preguntas desactivadas (activo = FALSE). Eso hacía imposible
--  el flujo que exige el trigger trg_proteger_texto_pregunta:
--  desactivar la pregunta con historial y crear una nueva con el
--  mismo número. Se reemplaza por un índice único parcial, igual
--  al patrón que ya usa idx_pregunta_activo.
-- ============================================================
ALTER TABLE pregunta DROP CONSTRAINT pregunta_cuestionario_id_numero_key;

CREATE UNIQUE INDEX uq_pregunta_cuestionario_numero_activa
    ON pregunta (cuestionario_id, numero)
    WHERE activo = TRUE;
