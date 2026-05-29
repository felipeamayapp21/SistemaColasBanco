package com.banco.controllers;

import com.banco.services.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/historial")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @GetMapping
    public java.util.List<?> getHistorial() {
        return historialService.getHistorial();
    }

    @GetMapping("/indicadores")
    public Map<String, Object> getIndicadores() {
        return historialService.getIndicadores();
    }

    @GetMapping("/stats/atenciones-por-dia")
    public java.util.List<Map<String, Object>> atencionesPorDia() {
        return historialService.atencionesPorDia();
    }

    @GetMapping("/stats/por-tramite")
    public java.util.List<Map<String, Object>> atencionesPorTramite() {
        return historialService.atencionesPorTramite();
    }

    @GetMapping("/stats/por-cajero")
    public java.util.List<Map<String, Object>> atencionesPorCajero() {
        return historialService.atencionesPorCajero();
    }
}
