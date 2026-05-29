package com.banco.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10, unique = true)
    private String ticket;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El documento es obligatorio")
    @Size(min = 1, max = 10, message = "El documento debe tener entre 1 y 10 digitos.")
    @Pattern(
        regexp = "^\\d{1,10}$",
        message = "El documento solo puede contener numeros y no puede superar los 10 digitos."
    )
    @Column(nullable = false, unique = true, length = 10)
    private String documento;

    @Min(value = 1, message = "La edad debe ser mayor a 0")
    @Column(nullable = false)
    private int edad;

    @Column(length = 20)
    private String genero = "";

    @Column(length = 255)
    private String direccion = "";

    @Column(length = 20)
    private String telefono = "";

    @Column(length = 100)
    private String correo = "";

    @Column(columnDefinition = "BYTEA")
    private byte[] foto;

    @Column(name = "ruta_foto", length = 255)
    private String rutaFoto;

    @Column(name = "tipo_cliente", nullable = false, length = 50)
    private String tipoCliente = "REGULAR";

    @Column(nullable = false)
    private boolean prioridad = false;

    @Column(name = "hora_ingreso", nullable = false)
    private LocalDateTime horaIngreso;

    @Column(nullable = false, length = 30)
    private String estado = "ESPERA";

    @PrePersist
    public void prePersist() {
        if (this.horaIngreso == null) {
            this.horaIngreso = LocalDateTime.now();
        }
        this.prioridad = this.edad > 60;
        this.tipoCliente = this.prioridad ? "PRIORITARIO" : "REGULAR";
    }
}
