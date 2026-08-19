-- ============================================================
--  Alcance del reporte — Etapa 5 de PROMPT_DASHBOARD_REPORTES.md
--  Permite generar informes independientes por categoría o
--  subcategoría, además del informe integral de siempre. Aditiva:
--  los reportes ya generados quedan con ambas columnas en NULL,
--  que significa "alcance integral" (toda la auditoría) — mismo
--  comportamiento que tenían antes de esta migración.
-- ============================================================

ALTER TABLE reporte
    ADD COLUMN alcance_tipo   VARCHAR(20) CHECK (alcance_tipo IN ('categoria', 'subcategoria')),
    ADD COLUMN alcance_nombre VARCHAR(150);

-- alcance_nombre solo tiene sentido junto con alcance_tipo (o ambos nulos, o ambos presentes).
ALTER TABLE reporte
    ADD CONSTRAINT chk_reporte_alcance_consistente
    CHECK ((alcance_tipo IS NULL) = (alcance_nombre IS NULL));
