package com.banco.repositories;

import com.banco.models.HistorialAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface HistorialAtencionRepository extends JpaRepository<HistorialAtencion, Long> {

    List<HistorialAtencion> findAllByOrderByHoraAtencionDesc();

    List<HistorialAtencion> findByClienteIdOrderByHoraAtencionDesc(Long clienteId);

    @Query("SELECT AVG(h.tiempoEspera) FROM HistorialAtencion h")
    Double promedioTiempoEspera();

    @Query("SELECT COUNT(h) FROM HistorialAtencion h WHERE h.cliente.prioridad = true")
    Long totalPrioritariosAtendidos();

    @Query(value = "SELECT DATE(h.hora_atencion) as dia, COUNT(*) FROM historial_atencion h GROUP BY dia ORDER BY dia", nativeQuery = true)
    List<Object[]> atencionesPorDia();

    @Query("SELECT h.tipoTramite, COUNT(h) FROM HistorialAtencion h WHERE h.tipoTramite IS NOT NULL AND h.tipoTramite <> '' GROUP BY h.tipoTramite ORDER BY COUNT(h) DESC")
    List<Object[]> atencionesPorTramite();

    @Query("SELECT h.cajeroUsuario, COUNT(h) FROM HistorialAtencion h WHERE h.cajeroUsuario IS NOT NULL GROUP BY h.cajeroUsuario ORDER BY COUNT(h) DESC")
    List<Object[]> atencionesPorCajero();
}
