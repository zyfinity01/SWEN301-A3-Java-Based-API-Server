package nz.ac.wgtn.swen301.restappender.server;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;

public class StatsExcelServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Logger Statistics");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"logger", "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(headers[i]);
        }

        LinkedHashMap<String, LinkedHashMap<String, Integer>> DB_TABLE = Persistency.generateStats();
        int rowNum = 1;
        for (String loggerName : DB_TABLE.keySet()) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(loggerName);

            LinkedHashMap<String, Integer> loggerStats = DB_TABLE.get(loggerName);
            int cellNum = 1;
            for (String logLevel : loggerStats.keySet()) {
                row.createCell(cellNum++).setCellValue(loggerStats.get(logLevel));
            }
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setHeader("Content-Disposition", "attachment; filename=LoggerStatistics.xlsx");
        workbook.write(resp.getOutputStream());

        workbook.close();
    }
}
