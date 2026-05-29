-- ============================================================
--  ProyectoBancario — Schema de Base de Datos
--  Base de datos: PostgreSQL
-- ============================================================

-- Crear base de datos (ejecutar conectado a postgres)
-- CREATE DATABASE banco_db;
-- \c banco_db;

-- ────────────────────────────────────────────────────────────
-- TABLA: usuarios (para login del sistema)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS usuarios (
    id                  SERIAL PRIMARY KEY,
    username            VARCHAR(50)  UNIQUE NOT NULL,
    password            VARCHAR(255) NOT NULL,
    rol                 VARCHAR(30)  NOT NULL DEFAULT 'CAJERO', -- ADMIN | CAJERO
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    tipo_recuperacion   VARCHAR(20),  -- PREGUNTA | PALABRA_CLAVE
    pregunta_seguridad  VARCHAR(255),
    respuesta_seguridad VARCHAR(255),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- ────────────────────────────────────────────────────────────
-- TABLA: clientes (cola de atencion)
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS clientes (
    id           SERIAL PRIMARY KEY,
    ticket       VARCHAR(10)  UNIQUE,
    nombre       VARCHAR(100) NOT NULL,
    documento    VARCHAR(10)  UNIQUE NOT NULL,
    edad         INT          NOT NULL CHECK (edad > 0),
    genero       VARCHAR(20)  DEFAULT '',
    direccion    VARCHAR(255) DEFAULT '',
    telefono     VARCHAR(20)  DEFAULT '',
    correo       VARCHAR(100) DEFAULT '',
    foto         BYTEA,
    ruta_foto    VARCHAR(255),
    tipo_cliente VARCHAR(50)  NOT NULL DEFAULT 'REGULAR', -- REGULAR | PRIORITARIO
    prioridad    BOOLEAN      NOT NULL DEFAULT FALSE,
    hora_ingreso TIMESTAMP    NOT NULL DEFAULT NOW(),
    estado       VARCHAR(30)  NOT NULL DEFAULT 'ESPERA'  -- ESPERA | EN_ATENCION | ATENDIDO | CANCELADO
);

-- Indices
CREATE INDEX IF NOT EXISTS idx_clientes_documento ON clientes(documento);
CREATE INDEX IF NOT EXISTS idx_clientes_estado ON clientes(estado);
CREATE INDEX IF NOT EXISTS idx_clientes_ticket ON clientes(ticket);
CREATE INDEX IF NOT EXISTS idx_clientes_nombre ON clientes(nombre);

-- ────────────────────────────────────────────────────────────
-- TABLA: historial_atencion
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS historial_atencion (
    id              SERIAL PRIMARY KEY,
    cliente_id      INT       NOT NULL,
    ticket          VARCHAR(10),
    hora_atencion   TIMESTAMP NOT NULL DEFAULT NOW(),
    hora_fin        TIMESTAMP,
    tiempo_espera   INT       NOT NULL DEFAULT 0, -- segundos entre ingreso y atencion
    duracion        INT       NOT NULL DEFAULT 0, -- segundos de atencion
    cajero_usuario  VARCHAR(50),
    motivo_visita   VARCHAR(255) DEFAULT '',
    tipo_tramite    VARCHAR(100) DEFAULT '',
    estado_atencion VARCHAR(30)  DEFAULT 'COMPLETADO', -- EN_ATENCION | COMPLETADO | CANCELADO
    observaciones   TEXT DEFAULT '',
    FOREIGN KEY (cliente_id) REFERENCES clientes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_historial_cliente ON historial_atencion(cliente_id);
CREATE INDEX IF NOT EXISTS idx_historial_fecha   ON historial_atencion(hora_atencion);
CREATE INDEX IF NOT EXISTS idx_historial_ticket   ON historial_atencion(ticket);
