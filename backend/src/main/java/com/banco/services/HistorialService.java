package com.banco.services;

import com.banco.models.HistorialAtencion;
import com.banco.repositories.HistorialAtencionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistorialService {

    private final HistorialAtencionRepository historialRepo;

    public List<HistorialAtencion> getHistorial() {
        return historialRepo.findAllByOrderByHoraAtencionDesc();
    }

    public Map<String, Object> getIndicadores() {
        Double promedio  = historialRepo.promedioTiempoEspera();
        Long   totalAten = historialRepo.count();
        Long   priorit   = historialRepo.totalPrioritariosAtendidos();

        return Map.of(
            "totalAtendidos",          totalAten,
            "tiempoPromedioSegundos",  promedio  != null ? Math.round(promedio) : 0,
            "prioritariosAtendidos",   priorit   != null ? priorit : 0
        );
    }

    public java.util.List<Map<String, Object>> atencionesPorDia() {
        return historialRepo.atencionesPorDia().stream()
            .map(r -> Map.<String, Object>of("dia", r[0] != null ? r[0].toString() : "", "total", ((Number) r[1]).longValue()))
            .toList();
    }

    public java.util.List<Map<String, Object>> atencionesPorTramite() {
        return historialRepo.atencionesPorTramite().stream()
            .map(r -> Map.<String, Object>of("tramite", r[0] != null ? r[0].toString() : "Sin especificar", "total", ((Number) r[1]).longValue()))
            .toList();
    }

    public java.util.List<Map<String, Object>> atencionesPorCajero() {
        return historialRepo.atencionesPorCajero().stream()
            .map(r -> Map.<String, Object>of("cajero", r[0] != null ? r[0].toString() : "Sistema", "total", ((Number) r[1]).longValue()))
            .toList();
    }
}
