package com.banco.models;

import jakarta.persistence.*;
import lombok.*;

/**
 * Usuario — Entidad JPA para el login del sistema.
 */
@Entity
@Table(name = "usuarios")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 30)
    private String rol; // ADMIN | CAJERO

    @Column(nullable = false)
    private boolean activo = true;

    /** PREGUNTA o PALABRA_CLAVE — tipo de recuperación configurado por el usuario */
    @Column(name = "tipo_recuperacion", length = 20)
    private String tipoRecuperacion;

    /** Texto de la pregunta de seguridad o etiqueta de palabra clave */
    @Column(name = "pregunta_seguridad", length = 255)
    private String preguntaSeguridad;

    /** Respuesta secreta (pregunta de seguridad o palabra clave) */
    @Column(name = "respuesta_seguridad", length = 255)
    private String respuestaSeguridad;
}
