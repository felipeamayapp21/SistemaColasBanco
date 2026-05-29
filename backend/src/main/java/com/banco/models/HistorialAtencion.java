package com.banco.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_atencion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class HistorialAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(length = 10)
    private String ticket;

    @Column(name = "hora_atencion", nullable = false)
    private LocalDateTime horaAtencion;

    @Column(name = "hora_fin")
    private LocalDateTime horaFin;

    @Column(name = "tiempo_espera", nullable = false)
    private int tiempoEspera;

    @Column(nullable = false)
    private int duracion;

    @Column(name = "cajero_usuario", length = 50)
    private String cajeroUsuario;

    @Column(name = "motivo_visita", length = 255)
    private String motivoVisita = "";

    @Column(name = "tipo_tramite", length = 100)
    private String tipoTramite = "";

    @Column(name = "estado_atencion", length = 30)
    private String estadoAtencion = "COMPLETADO";

    @Column(columnDefinition = "TEXT")
    private String observaciones = "";

    @PrePersist
    public void prePersist() {
        if (this.horaAtencion == null) {
            this.horaAtencion = LocalDateTime.now();
        }
        if (cliente != null && cliente.getHoraIngreso() != null) {
            long segundos = java.time.Duration
                .between(cliente.getHoraIngreso(), this.horaAtencion)
                .getSeconds();
            this.tiempoEspera = (int) Math.max(0, segundos);
        }
        if (this.ticket == null && cliente != null) {
            this.ticket = cliente.getTicket();
        }
    }
}
