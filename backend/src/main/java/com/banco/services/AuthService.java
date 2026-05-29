package com.banco.services;

import com.banco.models.Usuario;
import com.banco.repositories.UsuarioRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Autentica usuario y retorna JWT + datos del usuario.
     * Lanza excepción si credenciales incorrectas.
     */
    public Map<String, Object> login(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("Usuario y contraseña son requeridos.");
        }
        
        String cleanUsername = username.trim();
        Usuario usuario = usuarioRepo.findByUsernameIgnoreCase(cleanUsername)
            .orElseThrow(() -> new IllegalArgumentException("El usuario '" + cleanUsername + "' no existe en la base de datos."));

        if (!usuario.isActivo()) {
            throw new IllegalStateException("Usuario desactivado.");
        }
        boolean coincide = passwordEncoder.matches(password, usuario.getPassword());
        log.debug("Login '{}': hash={}, coincide={}", cleanUsername, usuario.getPassword(), coincide);
        if (!coincide) {
            throw new IllegalArgumentException("Contraseña incorrecta.");
        }

        String token = generarToken(usuario);

        return Map.of(
            "token", token,
            "user", Map.of(
                "id",       usuario.getId(),
                "username", usuario.getUsername(),
                "rol",      usuario.getRol()
            )
        );
    }

    /** Registra un nuevo cajero con datos de recuperación de contraseña. */
    public Map<String, Object> register(
            String username,
            String password,
            String tipoRecuperacion,
            String preguntaSeguridad,
            String respuestaSeguridad) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            throw new IllegalStateException("Usuario y contraseña no pueden estar vacíos.");
        }

        validarDatosRecuperacion(tipoRecuperacion, preguntaSeguridad, respuestaSeguridad);

        String cleanUsername = username.trim();
        if (usuarioRepo.existsByUsernameIgnoreCase(cleanUsername)) {
            throw new IllegalStateException("El nombre de usuario '" + cleanUsername + "' ya está en uso.");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(cleanUsername);
        String encoded = passwordEncoder.encode(password);
        usuario.setPassword(encoded);
        log.debug("Register '{}': hash generado={}", cleanUsername, encoded);
        usuario.setTipoRecuperacion(tipoRecuperacion.trim().toUpperCase());
        usuario.setPreguntaSeguridad(preguntaSeguridad.trim());
        usuario.setRespuestaSeguridad(normalizarRespuesta(respuestaSeguridad));
        
        // El primer usuario registrado será ADMIN automáticamente
        if (usuarioRepo.count() == 0) {
            usuario.setRol("ADMIN");
        } else {
            usuario.setRol("CAJERO");
        }
        
        usuario.setActivo(true);
        usuarioRepo.save(usuario);

        String token = generarToken(usuario);

        return Map.of(
            "token", token,
            "user", Map.of(
                "id",       usuario.getId(),
                "username", usuario.getUsername(),
                "rol",      usuario.getRol()
            )
        );
    }

    /** Obtiene la pregunta o indicación de palabra clave para recuperación. */
    public Map<String, String> obtenerDatosRecuperacion(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es requerido.");
        }
        Usuario usuario = usuarioRepo.findByUsernameIgnoreCase(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (!usuario.isActivo()) {
            throw new IllegalStateException("Usuario desactivado.");
        }
        if (usuario.getPreguntaSeguridad() == null || usuario.getRespuestaSeguridad() == null) {
            throw new IllegalStateException(
                "Este usuario no tiene configurada una pregunta de seguridad o palabra clave.");
        }
        return Map.of(
            "tipoRecuperacion", usuario.getTipoRecuperacion() != null ? usuario.getTipoRecuperacion() : "PREGUNTA",
            "preguntaSeguridad", usuario.getPreguntaSeguridad()
        );
    }

    /** Restablece la contraseña tras validar la respuesta de seguridad. */
    public void recuperarPassword(String username, String respuesta, String nuevaPassword) {
        if (username == null || respuesta == null || nuevaPassword == null
                || username.trim().isEmpty() || respuesta.trim().isEmpty() || nuevaPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Usuario, respuesta y nueva contraseña son requeridos.");
        }
        if (nuevaPassword.trim().length() < 4) {
            throw new IllegalArgumentException("La nueva contraseña debe tener al menos 4 caracteres.");
        }

        Usuario usuario = usuarioRepo.findByUsernameIgnoreCase(username.trim())
            .orElseThrow(() -> new IllegalArgumentException("El usuario no existe."));
        if (!usuario.isActivo()) {
            throw new IllegalStateException("Usuario desactivado.");
        }
        if (usuario.getRespuestaSeguridad() == null) {
            throw new IllegalStateException(
                "Este usuario no tiene configurada una pregunta de seguridad o palabra clave.");
        }
        if (!normalizarRespuesta(respuesta).equals(usuario.getRespuestaSeguridad())) {
            throw new IllegalArgumentException("La respuesta de seguridad es incorrecta.");
        }

        usuario.setPassword(passwordEncoder.encode(nuevaPassword.trim()));
        usuarioRepo.save(usuario);
    }

    private void validarDatosRecuperacion(
            String tipoRecuperacion,
            String preguntaSeguridad,
            String respuestaSeguridad) {
        if (tipoRecuperacion == null || preguntaSeguridad == null || respuestaSeguridad == null
                || tipoRecuperacion.trim().isEmpty()
                || preguntaSeguridad.trim().isEmpty()
                || respuestaSeguridad.trim().isEmpty()) {
            throw new IllegalStateException(
                "Debe configurar una pregunta de seguridad o palabra clave para recuperar su contraseña.");
        }
        String tipo = tipoRecuperacion.trim().toUpperCase();
        if (!tipo.equals("PREGUNTA") && !tipo.equals("PALABRA_CLAVE")) {
            throw new IllegalArgumentException("El tipo de recuperación debe ser PREGUNTA o PALABRA_CLAVE.");
        }
    }

    private String normalizarRespuesta(String respuesta) {
        if (respuesta == null) {
            return "";
        }
        String normalized = java.text.Normalizer.normalize(respuesta.trim().toLowerCase(), java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String generarToken(Usuario usuario) {
        return Jwts.builder()
            .subject(usuario.getUsername())
            .claim("rol", usuario.getRol())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(signingKey())
            .compact();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
