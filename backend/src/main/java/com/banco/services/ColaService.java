package com.banco.services;

import com.banco.models.Cliente;
import com.banco.repositories.ClienteRepository;
import com.banco.utils.ColaPrioritaria;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ColaService â€” Gestiona la ColaPrioritaria en memoria.
 *
 * Al arrancar el servidor, sincroniza la cola con los clientes
 * en estado ESPERA que existan en la base de datos.
 *
 * La ColaPrioritaria usa dos ColaBancaria (lista enlazada manual):
 *   - colaPrioritaria â†’ adultos mayores (edad > 60)
 *   - colaRegular     â†’ resto de clientes
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ColaService {

    private final ClienteRepository clienteRepository;
    private final JdbcTemplate jdbcTemplate;

    // Cola en memoria â€” persiste mientras el servidor estÃ© activo
    private final ColaPrioritaria cola = new ColaPrioritaria();

    /**
     * Al iniciar el servidor, migra columnas faltantes (si las hay)
     * y luego carga desde BD todos los clientes con estado ESPERA
     * y los inserta en la cola en memoria.
     */
    @PostConstruct
    public void sincronizarDesdeDB() {
        migrarColumnas();

        List<Cliente> pendientes = clienteRepository
            .findByEstadoOrderByPrioridadDescHoraIngresoAsc("ESPERA");

        for (Cliente c : pendientes) {
            cola.ingresarCliente(c);
        }
        log.info("âœ… Cola sincronizada: {} cliente(s) en espera.", pendientes.size());
    }

    /** Agrega un cliente reciÃ©n registrado a la cola en memoria. */
    public void agregarCliente(Cliente cliente) {
        cola.ingresarCliente(cliente);
        log.info("âž• Cliente ingresado a la cola: {} (prioridad={})",
                 cliente.getNombre(), cliente.isPrioridad());
    }

    /**
     * Atiende al siguiente cliente de la cola.
     * Prioritarios primero; si no hay, regulares.
     * Retorna null si no hay nadie en espera.
     */
    public Cliente atenderSiguiente() {
        Cliente atendido = cola.atenderSiguiente();
        if (atendido != null) {
            log.info("âœ… Turno #{} â€” Atendiendo: {}", cola.getTurnoActual(), atendido.getNombre());
        } else {
            log.info("âš ï¸  No hay clientes en espera.");
        }
        return atendido;
    }

    /** Consulta el prÃ³ximo cliente sin sacarlo de la cola. */
    public Cliente verProximo() {
        return cola.verProximo();
    }

    /** Estado actual de la cola. */
    public int getTotalEnEspera()               { return cola.totalEnEspera(); }
    public int getPrioritariosEnEspera()        { return cola.clientesPrioritariosEnEspera(); }
    public int getRegularesEnEspera()           { return cola.clientesRegularesEnEspera(); }
    public int getTurnoActual()                 { return cola.getTurnoActual(); }

    private void migrarColumnas() {
        log.info("ðŸ”§ Ejecutando migraciÃ³n automÃ¡tica de columnas faltantes...");
        String[] migraciones = {
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS ticket VARCHAR(10)",
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS genero VARCHAR(20)",
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS direccion VARCHAR(255)",
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS telefono VARCHAR(20)",
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS correo VARCHAR(100)",
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS foto BYTEA",
            "ALTER TABLE clientes ADD COLUMN IF NOT EXISTS ruta_foto VARCHAR(255)",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS ticket VARCHAR(20)",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS hora_fin TIMESTAMP",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS duracion VARCHAR(20)",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS motivo_visita VARCHAR(255)",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS tipo_tramite VARCHAR(100)",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS estado_atencion VARCHAR(20)",
            "ALTER TABLE historial_atencion ADD COLUMN IF NOT EXISTS observaciones TEXT"
        };
        for (String sql : migraciones) {
            try {
                jdbcTemplate.execute(sql);
                log.debug("âœ… Ejecutado: {}", sql);
            } catch (Exception e) {
                log.warn("âš ï¸  No se pudo ejecutar (probablemente ya existe la columna): {} â€” {}", sql, e.getMessage());
            }
        }
        log.info("âœ… MigraciÃ³n automÃ¡tica completada.");
    }

    /** Remueve un cliente de la cola por ID. */
    public void removerCliente(Long id) {
        boolean removido = cola.removerPorId(id);
        if (removido) {
            log.info("âŒ Cliente con ID {} removido de la cola en memoria.", id);
        } else {
            log.warn("âš ï¸ No se pudo remover el cliente con ID {} de la cola en memoria (no encontrado).", id);
        }
    }
}
