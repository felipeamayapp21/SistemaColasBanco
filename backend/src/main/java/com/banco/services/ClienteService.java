package com.banco.services;

import com.banco.config.QueueWebSocketHandler;
import com.banco.models.Cliente;
import com.banco.models.HistorialAtencion;
import com.banco.repositories.ClienteRepository;
import com.banco.repositories.HistorialAtencionRepository;
import com.banco.utils.DocumentoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    @Value("${app.uploads.fotos:uploads/fotos}")
    private String uploadDir;

    private final ClienteRepository clienteRepo;
    private final HistorialAtencionRepository historialRepo;
    private final ColaService colaService;
    private final QueueWebSocketHandler wsHandler;

    private String generarTicket(boolean esPrioritario) {
        String prefijo = esPrioritario ? "P" : "G";
        Integer maxNum = clienteRepo.maxNumeroTicket(prefijo + "%");
        int next = (maxNum != null ? maxNum : 0) + 1;
        return prefijo + String.format("%03d", next);
    }

    @Transactional
    public Cliente registrar(Cliente cliente) {
        String documento = DocumentoValidator.normalizar(cliente.getDocumento());
        DocumentoValidator.validar(documento);
        cliente.setDocumento(documento);

        java.util.Optional<Cliente> existenteOpt = clienteRepo.findByDocumento(documento);
        Cliente guardado;

        if (existenteOpt.isPresent()) {
            Cliente existente = existenteOpt.get();
            if ("ESPERA".equals(existente.getEstado()) || "EN_ATENCION".equals(existente.getEstado())) {
                throw new IllegalStateException(
                    "El documento " + documento + " ya esta activo en la cola.");
            }
            existente.setNombre(cliente.getNombre());
            existente.setEdad(cliente.getEdad());
            existente.setEstado("ESPERA");
            existente.setHoraIngreso(LocalDateTime.now());
            existente.setPrioridad(cliente.getEdad() > 60);
            existente.setTipoCliente(existente.isPrioridad() ? "PRIORITARIO" : "REGULAR");
            if (cliente.getGenero() != null) existente.setGenero(cliente.getGenero());
            if (cliente.getDireccion() != null) existente.setDireccion(cliente.getDireccion());
            if (cliente.getTelefono() != null) existente.setTelefono(cliente.getTelefono());
            if (cliente.getCorreo() != null) existente.setCorreo(cliente.getCorreo());
            if (cliente.getFoto() != null) existente.setFoto(cliente.getFoto());
            existente.setTicket(generarTicket(existente.isPrioridad()));
            guardado = clienteRepo.save(existente);
        } else {
            cliente.setTicket(generarTicket(cliente.getEdad() > 60));
            guardado = clienteRepo.save(cliente);
        }

        colaService.agregarCliente(guardado);
        broadcastEstado();
        return guardado;
    }

    @Transactional
    public Cliente llamarSiguiente() {
        Cliente enAtencion = getClienteEnAtencion();
        if (enAtencion != null) {
            throw new IllegalStateException(
                "Ya hay un cliente en atencion: " + enAtencion.getTicket() + " - " + enAtencion.getNombre()
            );
        }
        Cliente siguiente = colaService.atenderSiguiente();
        if (siguiente == null) {
            throw new IllegalStateException("No hay clientes en espera.");
        }
        siguiente.setEstado("EN_ATENCION");
        clienteRepo.save(siguiente);

        HistorialAtencion registro = new HistorialAtencion();
        registro.setCliente(siguiente);
        registro.setTicket(siguiente.getTicket());
        registro.setCajeroUsuario("sistema");
        registro.setEstadoAtencion("EN_ATENCION");
        historialRepo.save(registro);

        broadcastEstado();
        return siguiente;
    }

    @Transactional
    public HistorialAtencion iniciarAtencion(Long clienteId, String cajeroUsername) {
        Cliente c = clienteRepo.findById(clienteId)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + clienteId));

        if (!"EN_ATENCION".equals(c.getEstado())) {
            throw new IllegalStateException("El cliente no esta en estado EN_ATENCION.");
        }

        HistorialAtencion historial = historialRepo.findByClienteIdOrderByHoraAtencionDesc(clienteId)
            .stream()
            .filter(h -> "EN_ATENCION".equals(h.getEstadoAtencion()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No hay registro de atencion activo."));

        historial.setCajeroUsuario(cajeroUsername);
        historialRepo.save(historial);

        return historial;
    }

    @Transactional
    public HistorialAtencion finalizarAtencion(Long clienteId, String motivo, String tipoTramite,
                                                String observaciones, Integer duracionSegundos) {
        Cliente c = clienteRepo.findById(clienteId)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + clienteId));

        if (!"EN_ATENCION".equals(c.getEstado())) {
            throw new IllegalStateException("El cliente no esta en estado EN_ATENCION.");
        }

        HistorialAtencion historial = historialRepo.findByClienteIdOrderByHoraAtencionDesc(clienteId)
            .stream()
            .filter(h -> "EN_ATENCION".equals(h.getEstadoAtencion()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No hay registro de atencion activo."));

        LocalDateTime ahora = LocalDateTime.now();
        historial.setHoraFin(ahora);
        historial.setEstadoAtencion("COMPLETADO");
        if (motivo != null) historial.setMotivoVisita(motivo);
        if (tipoTramite != null) historial.setTipoTramite(tipoTramite);
        if (observaciones != null) historial.setObservaciones(observaciones);
        if (duracionSegundos != null && duracionSegundos > 0) {
            historial.setDuracion(duracionSegundos);
        } else if (historial.getHoraAtencion() != null) {
            long dur = Duration.between(historial.getHoraAtencion(), ahora).getSeconds();
            historial.setDuracion((int) Math.max(0, dur));
        }

        historialRepo.save(historial);

        c.setEstado("ATENDIDO");
        clienteRepo.save(c);
        broadcastEstado();
        return historial;
    }

    @Transactional
    public void cancelar(Long id) {
        Cliente c = clienteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));

        if ("EN_ATENCION".equals(c.getEstado())) {
            HistorialAtencion historial = historialRepo.findByClienteIdOrderByHoraAtencionDesc(id)
                .stream()
                .filter(h -> "EN_ATENCION".equals(h.getEstadoAtencion()))
                .findFirst()
                .orElse(null);
            if (historial != null) {
                historial.setHoraFin(LocalDateTime.now());
                historial.setEstadoAtencion("CANCELADO");
                historialRepo.save(historial);
            }
        }

        c.setEstado("CANCELADO");
        clienteRepo.save(c);
        colaService.removerCliente(id);
        broadcastEstado();
    }

    public List<Cliente> getCola() {
        return clienteRepo.findByEstadoOrderByPrioridadDescHoraIngresoAsc("ESPERA");
    }

    public List<Cliente> getPrioritarios() {
        return clienteRepo.findPrioritariosEnEspera();
    }

    public List<Cliente> getRegulares() {
        return clienteRepo.findRegularesEnEspera();
    }

    public Cliente getClienteEnAtencion() {
        List<Cliente> enAtencion = clienteRepo.findByEstadoOrderByPrioridadDescHoraIngresoAsc("EN_ATENCION");
        return enAtencion.isEmpty() ? null : enAtencion.get(0);
    }

    public Map<String, Object> getEstadoCola() {
        Cliente enAtencion = getClienteEnAtencion();
        Cliente proximo = colaService.verProximo();
        Map<String, Object> estado = new java.util.HashMap<>();
        estado.put("totalEnEspera",        colaService.getTotalEnEspera());
        estado.put("prioritariosEnEspera", colaService.getPrioritariosEnEspera());
        estado.put("regularesEnEspera",    colaService.getRegularesEnEspera());
        estado.put("turnoActual",          colaService.getTurnoActual());
        estado.put("proximoCliente",       proximo != null ? proximo.getNombre() : "Ninguno");
        estado.put("enAtencion",           enAtencion != null ? Map.of(
                                                "id", enAtencion.getId(),
                                                "nombre", enAtencion.getNombre(),
                                                "ticket", enAtencion.getTicket()
                                           ) : null);
        return estado;
    }

    public List<Cliente> buscarClientes(String termino) {
        return clienteRepo.buscarTodos(termino);
    }

    public List<HistorialAtencion> getHistorialCliente(Long clienteId) {
        return historialRepo.findByClienteIdOrderByHoraAtencionDesc(clienteId);
    }

    private void broadcastEstado() {
        try {
            Map<String, Object> estado = new HashMap<>(getEstadoCola());
            estado.put("colaCompleta", getCola());
            estado.put("colaPrioritarios", getPrioritarios());
            estado.put("colaRegulares", getRegulares());
            wsHandler.broadcast(Map.of("tipo", "QUEUE_UPDATE", "data", estado));
        } catch (Exception e) {
            // silent
        }
    }

    public Cliente getClienteById(Long id) {
        return clienteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
    }

    @Transactional
    public void actualizarFoto(Long id, MultipartFile file) {
        Cliente c = clienteRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + id));
        try {
            byte[] foto = file.getBytes();
            c.setFoto(foto);

            Files.createDirectories(Path.of(uploadDir));

            String ext = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = id + "_" + System.currentTimeMillis() + ext;
            Path targetPath = Path.of(uploadDir, fileName);
            Files.write(targetPath, foto);
            c.setRutaFoto(fileName);

            log.info("Foto guardada en disco: {}", targetPath);
        } catch (IOException e) {
            log.warn("No se pudo guardar foto en disco, solo en BD: {}", e.getMessage());
        }
        clienteRepo.save(c);
    }
}
