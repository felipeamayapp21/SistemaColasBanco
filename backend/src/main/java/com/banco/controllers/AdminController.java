package com.banco.controllers;

import com.banco.models.Usuario;
import com.banco.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/usuarios")
    public List<Usuario> listarUsuarios() {
        return usuarioRepo.findAll();
    }

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            String rol = body.getOrDefault("rol", "CAJERO");
            if (username == null || password == null || username.isBlank() || password.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario y contraseña requeridos"));
            }
            if (usuarioRepo.existsByUsernameIgnoreCase(username.trim())) {
                return ResponseEntity.badRequest().body(Map.of("error", "El usuario ya existe"));
            }
            Usuario u = new Usuario();
            u.setUsername(username.trim());
            u.setPassword(passwordEncoder.encode(password));
            u.setRol("ADMIN".equalsIgnoreCase(rol) ? "ADMIN" : "CAJERO");
            u.setActivo(true);
            usuarioRepo.save(u);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario creado", "id", u.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/usuarios/{id}/estado")
    public ResponseEntity<?> toggleEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        try {
            Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            u.setActivo(body.getOrDefault("activo", true));
            usuarioRepo.save(u);
            return ResponseEntity.ok(Map.of("mensaje", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/usuarios/{id}/rol")
    public ResponseEntity<?> cambiarRol(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            String nuevoRol = body.get("rol");
            if (!"ADMIN".equals(nuevoRol) && !"CAJERO".equals(nuevoRol)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rol inválido"));
            }
            u.setRol(nuevoRol);
            usuarioRepo.save(u);
            return ResponseEntity.ok(Map.of("mensaje", "Rol actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/usuarios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        try {
            Usuario u = usuarioRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            if (usuarioRepo.count() <= 1) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puedes eliminar el último administrador"));
            }
            usuarioRepo.delete(u);
            return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
