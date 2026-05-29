package com.banco.controllers;

import com.banco.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Retorna: { "token": "...", "user": { "id", "username", "rol" } }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> creds) {
        try {
            String username = creds.get("username");
            String password = creds.get("password");
            Map<String, Object> resultado = authService.login(username, password);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error interno del servidor: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> creds) {
        try {
            Map<String, Object> resultado = authService.register(
                creds.get("username"),
                creds.get("password"),
                creds.get("tipoRecuperacion"),
                creds.get("preguntaSeguridad"),
                creds.get("respuestaSeguridad")
            );
            return ResponseEntity.ok(resultado);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error al registrar usuario: " + e.getMessage()));
        }
    }

    /**
     * POST /api/auth/recuperar/datos
     * Body: { "username": "..." }
     */
    @PostMapping("/recuperar/datos")
    public ResponseEntity<?> obtenerDatosRecuperacion(@RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(authService.obtenerDatosRecuperacion(body.get("username")));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/auth/recuperar/restablecer
     * Body: { "username", "respuesta", "nuevaPassword" }
     */
    @PostMapping("/recuperar/restablecer")
    public ResponseEntity<?> recuperarPassword(@RequestBody Map<String, String> body) {
        try {
            authService.recuperarPassword(
                body.get("username"),
                body.get("respuesta"),
                body.get("nuevaPassword")
            );
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña restablecida correctamente."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
