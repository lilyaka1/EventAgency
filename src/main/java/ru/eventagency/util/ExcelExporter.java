package ru.eventagency.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eventagency.model.Event;
import java.io.*;
import java.util.List;

public class ExcelExporter {
    public void exportEvents(List<Event> events, String fileName) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Event orders");
            String[] headers = {"ID", "Название", "Тип", "Статус", "Дата", "Бюджет", "Клиент", "Описание"};
            Row header = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                header.createCell(index).setCellValue(headers[index]);
            }
            for (int index = 0; index < events.size(); index++) {
                writeEventRow(sheet.createRow(index + 1), events.get(index));
            }
            for (int index = 0; index < headers.length; index++) {
                sheet.autoSizeColumn(index);
            }
            try (OutputStream output = new FileOutputStream(fileName)) {
                workbook.write(output);
            }
        }
    }

    private void writeEventRow(Row row, Event event) {
        row.createCell(0).setCellValue(event.getId());
        row.createCell(1).setCellValue(event.getTitle());
        row.createCell(2).setCellValue(event.getType().name());
        row.createCell(3).setCellValue(event.getStatus().name());
        row.createCell(4).setCellValue(event.getEventDate().toString());
        row.createCell(5).setCellValue(event.getBudget().doubleValue());
        row.createCell(6).setCellValue(event.getClientName());
        row.createCell(7).setCellValue(event.getDescription() == null ? "" : event.getDescription());
    }
}
