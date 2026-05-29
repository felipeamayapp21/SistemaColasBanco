package com.banco.config;

import com.banco.models.Usuario;
import com.banco.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void run(String... args) {
        int migrados = 0;
        for (Usuario u : usuarioRepo.findAll()) {
            String pw = u.getPassword();
            if (pw != null && !pw.startsWith("$2a$") && !pw.startsWith("$2b$") && !pw.startsWith("$2y$")) {
                String encoded = passwordEncoder.encode(pw);
                u.setPassword(encoded);
                usuarioRepo.saveAndFlush(u);
                log.info("Migrada contraseña para usuario: {}", u.getUsername());
                migrados++;
            }
        }
        if (migrados > 0) {
            log.info("Se migraron {} contraseñas de texto plano a BCrypt.", migrados);
        }
    }
}
