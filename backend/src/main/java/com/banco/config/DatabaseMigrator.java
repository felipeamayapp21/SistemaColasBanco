package com.banco.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DatabaseMigrator implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DatabaseMigrator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        migrarClientes();
        migrarHistorial();
    }

    private void migrarClientes() {
        String[] columnas = {
            "ADD COLUMN IF NOT EXISTS ticket VARCHAR(10) UNIQUE",
            "ADD COLUMN IF NOT EXISTS genero VARCHAR(20) DEFAULT ''",
            "ADD COLUMN IF NOT EXISTS direccion VARCHAR(255) DEFAULT ''",
            "ADD COLUMN IF NOT EXISTS telefono VARCHAR(20) DEFAULT ''",
            "ADD COLUMN IF NOT EXISTS correo VARCHAR(100) DEFAULT ''",
            "ADD COLUMN IF NOT EXISTS foto BYTEA",
            "ADD COLUMN IF NOT EXISTS ruta_foto VARCHAR(255)"
        };
        for (String col : columnas) {
            try {
                jdbc.execute("ALTER TABLE clientes " + col);
            } catch (Exception e) {
                log.warn("No se pudo migrar columna: {}", e.getMessage());
            }
        }
        log.info("Migracion columnas clientes completada.");
    }

    private void migrarHistorial() {
        String[] columnas = {
            "ADD COLUMN IF NOT EXISTS ticket VARCHAR(10)",
            "ADD COLUMN IF NOT EXISTS hora_fin TIMESTAMP",
            "ADD COLUMN IF NOT EXISTS duracion INT DEFAULT 0",
            "ADD COLUMN IF NOT EXISTS motivo_visita VARCHAR(255) DEFAULT ''",
            "ADD COLUMN IF NOT EXISTS tipo_tramite VARCHAR(100) DEFAULT ''",
            "ADD COLUMN IF NOT EXISTS estado_atencion VARCHAR(30) DEFAULT 'COMPLETADO'",
            "ADD COLUMN IF NOT EXISTS observaciones TEXT DEFAULT ''"
        };
        for (String col : columnas) {
            try {
                jdbc.execute("ALTER TABLE historial_atencion " + col);
            } catch (Exception e) {
                log.warn("No se pudo migrar columna: {}", e.getMessage());
            }
        }
        log.info("Migracion columnas historial_atencion completada.");
    }
}
