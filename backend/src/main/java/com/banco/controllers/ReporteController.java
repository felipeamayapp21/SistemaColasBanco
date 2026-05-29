package com.banco.controllers;

import com.banco.models.HistorialAtencion;
import com.banco.repositories.HistorialAtencionRepository;
import com.banco.services.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Slf4j
public class ReporteController {

    private final ReporteService reporteService;
    private final HistorialAtencionRepository historialRepo;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> descargarPdf() {
        try {
            List<HistorialAtencion> datos = historialRepo.findAllByOrderByHoraAtencionDesc();
            byte[] contents = reporteService.generarPdf(datos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "historial_atencion.pdf");

            return ResponseEntity.ok()
                .headers(headers)
                .body(contents);
        } catch (Exception e) {
            log.error("Error generando PDF", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> descargarExcel() {
        try {
            List<HistorialAtencion> datos = historialRepo.findAllByOrderByHoraAtencionDesc();
            byte[] contents = reporteService.generarExcel(datos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "historial_atencion.xlsx");

            return ResponseEntity.ok()
                .headers(headers)
                .body(contents);
        } catch (Exception e) {
            log.error("Error generando Excel", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
