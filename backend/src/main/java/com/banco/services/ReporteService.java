package com.banco.services;

import com.banco.models.HistorialAtencion;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReporteService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generarPdf(List<HistorialAtencion> datos) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, out);

        document.open();

        com.lowagie.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Paragraph title = new Paragraph("Historial de Atencion - Banco", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(11);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        String[] headers = {"Ticket", "Nombre", "Documento", "Tipo", "Hora Ingreso", "Hora Atencion", "Hora Fin", "Espera", "Duracion", "Tramite", "Cajero"};
        com.lowagie.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Color azulBanco = new Color(30, 58, 95);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(azulBanco);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(4);
            table.addCell(cell);
        }

        for (HistorialAtencion h : datos) {
            table.addCell(h.getTicket() != null ? h.getTicket() : "-");
            table.addCell(h.getCliente().getNombre());
            table.addCell(h.getCliente().getDocumento());
            table.addCell(h.getCliente().getTipoCliente());
            table.addCell(h.getCliente().getHoraIngreso().format(FORMATTER));
            table.addCell(h.getHoraAtencion().format(FORMATTER));
            table.addCell(h.getHoraFin() != null ? h.getHoraFin().format(FORMATTER) : "-");
            table.addCell(formatSegundos(h.getTiempoEspera()));
            table.addCell(formatSegundos(h.getDuracion()));
            table.addCell(h.getTipoTramite() != null && !h.getTipoTramite().isEmpty() ? h.getTipoTramite() : "-");
            table.addCell(h.getCajeroUsuario() != null ? h.getCajeroUsuario() : "-");
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }

    public byte[] generarExcel(List<HistorialAtencion> datos) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Historial");

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);

            String[] headers = {"Ticket", "Nombre", "Documento", "Tipo", "Hora Ingreso", "Hora Atencion", "Hora Fin", "Espera", "Duracion", "Tramite", "Cajero"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (HistorialAtencion h : datos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(h.getTicket() != null ? h.getTicket() : "-");
                row.createCell(1).setCellValue(h.getCliente().getNombre());
                row.createCell(2).setCellValue(h.getCliente().getDocumento());
                row.createCell(3).setCellValue(h.getCliente().getTipoCliente());
                row.createCell(4).setCellValue(h.getCliente().getHoraIngreso().format(FORMATTER));
                row.createCell(5).setCellValue(h.getHoraAtencion().format(FORMATTER));
                row.createCell(6).setCellValue(h.getHoraFin() != null ? h.getHoraFin().format(FORMATTER) : "-");
                row.createCell(7).setCellValue(formatSegundos(h.getTiempoEspera()));
                row.createCell(8).setCellValue(formatSegundos(h.getDuracion()));
                row.createCell(9).setCellValue(h.getTipoTramite() != null && !h.getTipoTramite().isEmpty() ? h.getTipoTramite() : "-");
                row.createCell(10).setCellValue(h.getCajeroUsuario() != null ? h.getCajeroUsuario() : "-");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String formatSegundos(int segs) {
        if (segs < 60) return segs + "s";
        int min = segs / 60;
        int s = segs % 60;
        return min + "m " + s + "s";
    }
}
