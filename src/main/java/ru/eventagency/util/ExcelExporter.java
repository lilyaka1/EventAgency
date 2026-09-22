package ru.eventagency.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.eventagency.model.Event;
import java.io.*;
import java.util.List;

public class ExcelExporter {
    public void exportEvents(List<Event> events, String fileName) throws IOException {
        try(Workbook workbook=new XSSFWorkbook()){
            Sheet sheet=workbook.createSheet("Event orders");
            String[] headers={"ID","Название","Тип","Статус","Дата","Бюджет","Клиент","Описание"};
            Row header=sheet.createRow(0);for(int i=0;i<headers.length;i++)header.createCell(i).setCellValue(headers[i]);
            for(int i=0;i<events.size();i++){Event e=events.get(i);Row row=sheet.createRow(i+1);row.createCell(0).setCellValue(e.getId());row.createCell(1).setCellValue(e.getTitle());row.createCell(2).setCellValue(e.getType().name());row.createCell(3).setCellValue(e.getStatus().name());row.createCell(4).setCellValue(e.getEventDate().toString());row.createCell(5).setCellValue(e.getBudget().doubleValue());row.createCell(6).setCellValue(e.getClientName());row.createCell(7).setCellValue(e.getDescription()==null?"":e.getDescription());}
            for(int i=0;i<headers.length;i++)sheet.autoSizeColumn(i);
            try(OutputStream out=new FileOutputStream(fileName)){workbook.write(out);}
        }
    }
}
