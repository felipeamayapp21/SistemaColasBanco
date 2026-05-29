package com.banco.controllers;

import com.banco.models.Cliente;
import com.banco.models.HistorialAtencion;
import com.banco.services.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Value("${app.uploads.fotos:uploads/fotos}")
    private String uploadDir;

    @GetMapping("/cola")
    public List<Cliente> getCola() {
        return clienteService.getCola();
    }

    @GetMapping("/prioritarios")
    public List<Cliente> getPrioritarios() {
        return clienteService.getPrioritarios();
    }

    @GetMapping("/regulares")
    public List<Cliente> getRegulares() {
        return clienteService.getRegulares();
    }

    @GetMapping("/estado")
    public Map<String, Object> getEstadoCola() {
        return clienteService.getEstadoCola();
    }

    @GetMapping("/en-atencion")
    public ResponseEntity<?> getClienteEnAtencion() {
        Cliente c = clienteService.getClienteEnAtencion();
        if (c == null) {
            return ResponseEntity.ok(Map.of("enAtencion", null));
        }
        return ResponseEntity.ok(Map.of("enAtencion", Map.of(
            "id", c.getId(),
            "nombre", c.getNombre(),
            "ticket", c.getTicket(),
            "documento", c.getDocumento()
        )));
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody Cliente cliente) {
        try {
            Cliente guardado = clienteService.registrar(cliente);
            return ResponseEntity.ok(guardado);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> subirFoto(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            clienteService.actualizarFoto(id, file);
            return ResponseEntity.ok(Map.of("mensaje", "Foto actualizada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al subir la foto: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/{id}/foto", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<?> getFoto(@PathVariable Long id) {
        try {
            Cliente c = clienteService.getClienteById(id);
            if (c.getRutaFoto() != null) {
                Path filePath = Path.of(uploadDir, c.getRutaFoto());
                if (Files.exists(filePath)) {
                    byte[] fileBytes = Files.readAllBytes(filePath);
                    String contentType = Files.probeContentType(filePath);
                    if (contentType == null) contentType = MediaType.IMAGE_JPEG_VALUE;
                    return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(fileBytes);
                }
            }
            if (c.getFoto() != null) {
                return ResponseEntity.ok(c.getFoto());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/llamar-siguiente")
    public ResponseEntity<?> llamarSiguiente(Authentication authentication) {
        try {
            Cliente siguiente = clienteService.llamarSiguiente();
            return ResponseEntity.ok(siguiente);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/iniciar-atencion")
    public ResponseEntity<?> iniciarAtencion(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "sistema";
            HistorialAtencion h = clienteService.iniciarAtencion(id, username);
            return ResponseEntity.ok(h);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/finalizar-atencion")
    public ResponseEntity<?> finalizarAtencion(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            String motivo = (String) body.getOrDefault("motivoVisita", "");
            String tipoTramite = (String) body.getOrDefault("tipoTramite", "");
            String observaciones = (String) body.getOrDefault("observaciones", "");
            Integer duracion = body.get("duracion") != null ? (Integer) body.get("duracion") : null;
            HistorialAtencion h = clienteService.finalizarAtencion(id, motivo, tipoTramite, observaciones, duracion);
            return ResponseEntity.ok(h);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelar(@PathVariable Long id) {
        try {
            clienteService.cancelar(id);
            return ResponseEntity.ok(Map.of("mensaje", "Cliente cancelado correctamente."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscar(@RequestParam("q") String termino) {
        try {
            List<Cliente> resultados = clienteService.buscarClientes(termino);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCliente(@PathVariable Long id) {
        try {
            Cliente c = clienteService.getClienteById(id);
            return ResponseEntity.ok(c);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tv")
    public ResponseEntity<?> getTV() {
        return ResponseEntity.ok(clienteService.getEstadoCola());
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<?> getHistorialCliente(@PathVariable Long id) {
        try {
            List<HistorialAtencion> historial = clienteService.getHistorialCliente(id);
            return ResponseEntity.ok(historial);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
