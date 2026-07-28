-- ============================================================
--  Esquema de Base de Datos v3 — Auditorías Industriales SAS
--  Motor: PostgreSQL
--  Basado en: diagrama_erd_v3.png
--  Normalización: 3FN (cada tabla con PK propia, FKs explícitas,
--  sin dependencias transitivas ni datos repetidos entre tablas)
--
--  Cambios respecto a v2:
--   1. Se agrega "activo" (BOOLEAN) a categoria, subcategoria y
--      pregunta, igual que ya existía en cuestionario y usuario.
--      Antes, un elemento con respuestas asociadas no se podía
--      borrar (por el ON DELETE RESTRICT hacia respuesta /
--      auditoria_cuestionario) ni tampoco desactivar, porque no
--      existía el campo. Con "activo" se puede sacar de circulación
--      sin destruir historial de auditorías ya realizadas.
--   2. Se agrega un trigger que impide modificar el texto de una
--      pregunta si ya tiene respuestas registradas, para que el
--      historial de auditorías cerradas no cambie de significado
--      retroactivamente. Si la pregunta necesita otro enunciado,
--      se desactiva y se crea una nueva.
--   3. Se agrega "codigo_postal" a empresa.
-- ============================================================

-- Extensión necesaria para generar UUID automáticamente
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- 1. USUARIO
--    Auditores y administradores del sistema.
-- ============================================================
CREATE TABLE usuario (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL,
    correo          VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    rol             VARCHAR(20)  NOT NULL DEFAULT 'auditor'
                       CHECK (rol IN ('auditor', 'admin', 'supervisor')),
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP    NOT NULL DEFAULT now()
);

-- ============================================================
-- 2. EMPRESA
--    Empresas auditadas (clientes).
-- ============================================================
CREATE TABLE empresa (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razon_social    VARCHAR(150) NOT NULL,
    nit             VARCHAR(20)  NOT NULL UNIQUE,
    sector          VARCHAR(100),
    num_empleados   INTEGER      CHECK (num_empleados >= 0),
    ciudad          VARCHAR(100),
    departamento    VARCHAR(100),
    codigo_postal   VARCHAR(10),   -- NUEVO
    contacto        VARCHAR(100),
    telefono        VARCHAR(20),
    correo          VARCHAR(100),
    creado_en       TIMESTAMP    NOT NULL DEFAULT now()
);

-- ============================================================
-- 3. CATEGORIA
--    Departamentos/áreas de auditoría (ej. Mantenimiento).
-- ============================================================
CREATE TABLE categoria (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL UNIQUE,
    descripcion     VARCHAR(255),
    icono           VARCHAR(50),
    es_plantilla    BOOLEAN      NOT NULL DEFAULT FALSE,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,   -- NUEVO
    creado_en       TIMESTAMP    NOT NULL DEFAULT now()
);

-- ============================================================
-- 4. SUBCATEGORIA
--    Subdivisiones dentro de una categoría.
--    Relación "contiene": categoria 1:N subcategoria.
-- ============================================================
CREATE TABLE subcategoria (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(100) NOT NULL,
    descripcion     VARCHAR(255),
    categoria_id    UUID         NOT NULL REFERENCES categoria(id) ON DELETE CASCADE,
    responsable     VARCHAR(100),
    es_plantilla    BOOLEAN      NOT NULL DEFAULT FALSE,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,   -- NUEVO
    creado_en       TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (categoria_id, nombre)
);

-- ============================================================
-- 5. CUESTIONARIO
--    Cuestionario evaluable dentro de una subcategoría.
--    Relación "agrupa": subcategoria 1:N cuestionario.
-- ============================================================
CREATE TABLE cuestionario (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre          VARCHAR(150) NOT NULL,
    num_preguntas   INTEGER      NOT NULL DEFAULT 0 CHECK (num_preguntas >= 0),
    subcategoria_id UUID         NOT NULL REFERENCES subcategoria(id) ON DELETE CASCADE,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (subcategoria_id, nombre)
);

-- ============================================================
-- 6. PREGUNTA
--    Pregunta individual de un cuestionario (escala 1-5 + evidencia).
--    Relación "incluye": cuestionario 1:N pregunta.
-- ============================================================
CREATE TABLE pregunta (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero          INTEGER      NOT NULL CHECK (numero > 0),
    texto           VARCHAR(500) NOT NULL,
    evidencia       VARCHAR(255),
    cuestionario_id UUID         NOT NULL REFERENCES cuestionario(id) ON DELETE CASCADE,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,   -- NUEVO
    UNIQUE (cuestionario_id, numero)
);

-- ============================================================
-- TRIGGER: proteger el texto de preguntas ya respondidas
--    Si una pregunta ya tiene respuestas registradas, no se
--    permite cambiar su enunciado (texto), para que el historial
--    de auditorías cerradas no quede alterado retroactivamente.
--    Sí se puede seguir cambiando "activo", "evidencia", "numero",
--    etc. Para reemplazar el enunciado, se desactiva la pregunta
--    (activo = FALSE) y se crea una nueva.
-- ============================================================
CREATE OR REPLACE FUNCTION fn_proteger_texto_pregunta()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.texto IS DISTINCT FROM OLD.texto THEN
        IF EXISTS (SELECT 1 FROM respuesta WHERE pregunta_id = OLD.id) THEN
            RAISE EXCEPTION
                'No se puede modificar el texto de la pregunta % porque ya tiene respuestas registradas. Desactívela (activo = FALSE) y cree una nueva.',
                OLD.id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_proteger_texto_pregunta
BEFORE UPDATE ON pregunta
FOR EACH ROW
EXECUTE FUNCTION fn_proteger_texto_pregunta();

-- ============================================================
-- TRIGGER: sincronizar cuestionario.num_preguntas
--    Mantiene num_preguntas siempre igual al conteo real de filas
--    en "pregunta", sin importar si la pregunta se agrega, borra,
--    o se mueve a otro cuestionario. Así num_preguntas nunca queda
--    desincronizado del contenido real, sin depender de que la
--    aplicación (JS, otro sistema, etc.) lo actualice a mano.
-- ============================================================
CREATE OR REPLACE FUNCTION fn_sync_num_preguntas()
RETURNS TRIGGER AS $$
BEGIN
    -- INSERT: recalcular el cuestionario de la fila nueva
    IF TG_OP = 'INSERT' THEN
        UPDATE cuestionario
           SET num_preguntas = (SELECT count(*) FROM pregunta WHERE cuestionario_id = NEW.cuestionario_id)
         WHERE id = NEW.cuestionario_id;
        RETURN NEW;
    END IF;

    -- DELETE: recalcular el cuestionario de la fila eliminada
    IF TG_OP = 'DELETE' THEN
        UPDATE cuestionario
           SET num_preguntas = (SELECT count(*) FROM pregunta WHERE cuestionario_id = OLD.cuestionario_id)
         WHERE id = OLD.cuestionario_id;
        RETURN OLD;
    END IF;

    -- UPDATE: si la pregunta cambió de cuestionario, recalcular AMBOS
    -- (el de origen y el de destino); si no cambió, solo el actual.
    IF TG_OP = 'UPDATE' THEN
        UPDATE cuestionario
           SET num_preguntas = (SELECT count(*) FROM pregunta WHERE cuestionario_id = NEW.cuestionario_id)
         WHERE id = NEW.cuestionario_id;

        IF OLD.cuestionario_id IS DISTINCT FROM NEW.cuestionario_id THEN
            UPDATE cuestionario
               SET num_preguntas = (SELECT count(*) FROM pregunta WHERE cuestionario_id = OLD.cuestionario_id)
             WHERE id = OLD.cuestionario_id;
        END IF;
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_num_preguntas
AFTER INSERT OR UPDATE OR DELETE ON pregunta
FOR EACH ROW
EXECUTE FUNCTION fn_sync_num_preguntas();

-- ============================================================
-- 7. AUDITORIA
--    Proceso de auditoría realizado a una empresa por un auditor.
--    Relaciones "tiene" (empresa) y "realiza" (usuario/auditor).
-- ============================================================
CREATE TABLE auditoria (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    empresa_id      UUID         NOT NULL REFERENCES empresa(id) ON DELETE RESTRICT,
    auditor_id      UUID         NOT NULL REFERENCES usuario(id)  ON DELETE RESTRICT,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'en_progreso'
                       CHECK (estado IN ('en_progreso', 'finalizada', 'cancelada')),
    puntaje_global  NUMERIC(5,2) CHECK (puntaje_global >= 0),
    fecha_inicio    DATE         NOT NULL DEFAULT CURRENT_DATE,
    fecha_fin       DATE,
    creado_en       TIMESTAMP    NOT NULL DEFAULT now(),
    CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio)
);

-- ============================================================
-- 8. AUDITORIA_CUESTIONARIO
--    Tabla puente: qué cuestionarios se aplican en cada auditoría
--    y el puntaje obtenido en cada uno.
--    Relaciones "aplica" (auditoria) y "usado en" (cuestionario).
-- ============================================================
CREATE TABLE auditoria_cuestionario (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditoria_id    UUID         NOT NULL REFERENCES auditoria(id)    ON DELETE CASCADE,
    cuestionario_id UUID         NOT NULL REFERENCES cuestionario(id) ON DELETE RESTRICT,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'pendiente'
                       CHECK (estado IN ('pendiente', 'en_progreso', 'completado')),
    puntaje         NUMERIC(5,2) CHECK (puntaje >= 0),
    creado_en       TIMESTAMP    NOT NULL DEFAULT now(),
    UNIQUE (auditoria_id, cuestionario_id)
);

-- ============================================================
-- 9. RESPUESTA
--    Respuesta puntual a una pregunta, dentro de un
--    auditoria_cuestionario específico.
--    Relaciones "registra" (auditoria_cuestionario) y "recibe" (pregunta).
-- ============================================================
CREATE TABLE respuesta (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditoria_cuestionario_id   UUID      NOT NULL REFERENCES auditoria_cuestionario(id) ON DELETE CASCADE,
    pregunta_id                 UUID      NOT NULL REFERENCES pregunta(id)               ON DELETE RESTRICT,
    valor                       INTEGER   NOT NULL CHECK (valor BETWEEN 1 AND 5),
    observacion                 VARCHAR(500),
    respondido_en               TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (auditoria_cuestionario_id, pregunta_id)
);

-- ============================================================
-- TRIGGER: sincronizar auditoria_cuestionario.puntaje
--    Cada vez que se inserta, actualiza o borra una respuesta,
--    recalcula el puntaje del auditoria_cuestionario correspondiente
--    como el promedio de los valores (escala 1-5) respondidos.
--    Si una respuesta se mueve a otro auditoria_cuestionario, se
--    recalculan ambos (origen y destino).
-- ============================================================
CREATE OR REPLACE FUNCTION fn_sync_puntaje_cuestionario()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE auditoria_cuestionario
           SET puntaje = (SELECT round(avg(valor), 2) FROM respuesta WHERE auditoria_cuestionario_id = NEW.auditoria_cuestionario_id)
         WHERE id = NEW.auditoria_cuestionario_id;
        RETURN NEW;
    END IF;

    IF TG_OP = 'DELETE' THEN
        UPDATE auditoria_cuestionario
           SET puntaje = (SELECT round(avg(valor), 2) FROM respuesta WHERE auditoria_cuestionario_id = OLD.auditoria_cuestionario_id)
         WHERE id = OLD.auditoria_cuestionario_id;
        RETURN OLD;
    END IF;

    IF TG_OP = 'UPDATE' THEN
        UPDATE auditoria_cuestionario
           SET puntaje = (SELECT round(avg(valor), 2) FROM respuesta WHERE auditoria_cuestionario_id = NEW.auditoria_cuestionario_id)
         WHERE id = NEW.auditoria_cuestionario_id;

        IF OLD.auditoria_cuestionario_id IS DISTINCT FROM NEW.auditoria_cuestionario_id THEN
            UPDATE auditoria_cuestionario
               SET puntaje = (SELECT round(avg(valor), 2) FROM respuesta WHERE auditoria_cuestionario_id = OLD.auditoria_cuestionario_id)
             WHERE id = OLD.auditoria_cuestionario_id;
        END IF;
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_puntaje_cuestionario
AFTER INSERT OR UPDATE OR DELETE ON respuesta
FOR EACH ROW
EXECUTE FUNCTION fn_sync_puntaje_cuestionario();

-- ============================================================
-- TRIGGER: sincronizar auditoria.puntaje_global
--    Cada vez que cambia el puntaje de un auditoria_cuestionario
--    (lo cual ocurre automáticamente por el trigger anterior),
--    recalcula el puntaje_global de la auditoría como el promedio
--    de los puntajes de todos sus cuestionarios aplicados.
-- ============================================================
CREATE OR REPLACE FUNCTION fn_sync_puntaje_global()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE auditoria
           SET puntaje_global = (SELECT round(avg(puntaje), 2) FROM auditoria_cuestionario WHERE auditoria_id = NEW.auditoria_id AND puntaje IS NOT NULL)
         WHERE id = NEW.auditoria_id;
        RETURN NEW;
    END IF;

    IF TG_OP = 'DELETE' THEN
        UPDATE auditoria
           SET puntaje_global = (SELECT round(avg(puntaje), 2) FROM auditoria_cuestionario WHERE auditoria_id = OLD.auditoria_id AND puntaje IS NOT NULL)
         WHERE id = OLD.auditoria_id;
        RETURN OLD;
    END IF;

    IF TG_OP = 'UPDATE' THEN
        UPDATE auditoria
           SET puntaje_global = (SELECT round(avg(puntaje), 2) FROM auditoria_cuestionario WHERE auditoria_id = NEW.auditoria_id AND puntaje IS NOT NULL)
         WHERE id = NEW.auditoria_id;

        IF OLD.auditoria_id IS DISTINCT FROM NEW.auditoria_id THEN
            UPDATE auditoria
               SET puntaje_global = (SELECT round(avg(puntaje), 2) FROM auditoria_cuestionario WHERE auditoria_id = OLD.auditoria_id AND puntaje IS NOT NULL)
             WHERE id = OLD.auditoria_id;
        END IF;
        RETURN NEW;
    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sync_puntaje_global
AFTER INSERT OR UPDATE OR DELETE ON auditoria_cuestionario
FOR EACH ROW
EXECUTE FUNCTION fn_sync_puntaje_global();

-- ============================================================
-- 10. REPORTE
--     Reporte final generado a partir de una auditoría.
--     Relación "genera": auditoria 1:N reporte.
-- ============================================================
CREATE TABLE reporte (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditoria_id    UUID         NOT NULL REFERENCES auditoria(id) ON DELETE CASCADE,
    puntaje_total   NUMERIC(5,2) CHECK (puntaje_total >= 0),
    nivel_madurez   VARCHAR(50),
    ruta_pdf        VARCHAR(255),
    generado_en     TIMESTAMP    NOT NULL DEFAULT now()
);

-- ============================================================
-- 11. HALLAZGO
--     Hallazgos/no conformidades detectados durante una auditoría.
--     Relación "produce": auditoria 1:N hallazgo.
-- ============================================================
CREATE TABLE hallazgo (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditoria_id        UUID         NOT NULL REFERENCES auditoria(id) ON DELETE CASCADE,
    descripcion         VARCHAR(500) NOT NULL,
    severidad           VARCHAR(20)  NOT NULL DEFAULT 'media'
                           CHECK (severidad IN ('baja', 'media', 'alta', 'critica')),
    accion_recomendada  VARCHAR(500),
    estado              VARCHAR(20)  NOT NULL DEFAULT 'abierto'
                           CHECK (estado IN ('abierto', 'en_tratamiento', 'cerrado')),
    creado_en           TIMESTAMP    NOT NULL DEFAULT now()
);

-- ============================================================
--  ÍNDICES — aceleran los JOIN y filtros más frecuentes
-- ============================================================
CREATE INDEX idx_subcategoria_categoria        ON subcategoria(categoria_id);
CREATE INDEX idx_cuestionario_subcategoria      ON cuestionario(subcategoria_id);
CREATE INDEX idx_pregunta_cuestionario          ON pregunta(cuestionario_id);
CREATE INDEX idx_auditoria_empresa              ON auditoria(empresa_id);
CREATE INDEX idx_auditoria_auditor              ON auditoria(auditor_id);
CREATE INDEX idx_auditoria_cuestionario_auditoria   ON auditoria_cuestionario(auditoria_id);
CREATE INDEX idx_auditoria_cuestionario_cuestionario ON auditoria_cuestionario(cuestionario_id);
CREATE INDEX idx_respuesta_auditoria_cuestionario   ON respuesta(auditoria_cuestionario_id);
CREATE INDEX idx_respuesta_pregunta             ON respuesta(pregunta_id);
CREATE INDEX idx_reporte_auditoria              ON reporte(auditoria_id);
CREATE INDEX idx_hallazgo_auditoria             ON hallazgo(auditoria_id);

-- Índices parciales: filtrar rápido solo los elementos activos del
-- catálogo (uso típico al armar un nuevo cuestionario para una auditoría)
CREATE INDEX idx_categoria_activo     ON categoria(id)     WHERE activo = TRUE;
CREATE INDEX idx_subcategoria_activo  ON subcategoria(id)  WHERE activo = TRUE;
CREATE INDEX idx_pregunta_activo      ON pregunta(id)      WHERE activo = TRUE;
