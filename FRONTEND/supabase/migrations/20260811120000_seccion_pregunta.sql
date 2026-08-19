-- ============================================================
--  Migración — Auditorías Industriales SAS
--  Agrega pregunta.seccion: agrupación interna de preguntas
--  dentro de un cuestionario (2 a 8 secciones típicas por
--  checklist/PDF), usada para el ranking de barras a nivel de
--  sección en el módulo de resultados gráficos. Nullable y sin
--  backfill forzado: las preguntas existentes sin sección
--  asignada se agrupan como "General" en las consultas.
-- ============================================================

ALTER TABLE pregunta ADD COLUMN seccion VARCHAR(100);
