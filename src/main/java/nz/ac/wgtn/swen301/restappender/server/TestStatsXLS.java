package nz.ac.wgtn.swen301.restappender.server;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStatsXLS {

    private StatsExcelServlet servlet;

    @BeforeEach
    public void setup() {
        this.servlet = new StatsExcelServlet();
        try {
            deleteAllLogs();
            addMockLogs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addMockLogs() throws Exception {
        String[] levels = { "DEBUG", "INFO", "ERROR", "TRACE", "WARN", "FATAL" };
        String[] loggers = { "com.example.Logger1", "com.example.Logger2" };

        for (int i = 0; i < 20; i++) {
            String level = levels[i % levels.length];
            String logger = loggers[i % loggers.length];
            String postPayload = "{" +
                    "\"id\":\"" + UUID.randomUUID() + "\"," +
                    "\"message\":\"Mock log message " + i + "\"," +
                    "\"timestamp\":\"" + LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                    "\"thread\":\"main\"," +
                    "\"logger\":\"" + logger + "\"," +
                    "\"level\":\"" + level + "\"," +
                    "\"errorDetails\":\"mock error\"" +
                    "}";

            MockHttpServletRequest postRequest = new MockHttpServletRequest();
            postRequest.setMethod("POST");
            postRequest.setRequestURI("/restappender/logs");
            postRequest.setContentType("application/json");
            postRequest.setContent(postPayload.getBytes());

            MockHttpServletResponse postResponse = new MockHttpServletResponse();
            new LogsServlet().doPost(postRequest, postResponse);

            assertEquals(201, postResponse.getStatus());
        }
    }

    private void deleteAllLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("DELETE");
        request.setRequestURI("/restappender/logs");

        MockHttpServletResponse response = new MockHttpServletResponse();
        new LogsServlet().doDelete(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetStatsExcel() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/restappender/stats-excel");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

        // Use POI to parse the Excel content
        InputStream is = new ByteArrayInputStream(response.getContentAsByteArray());
        Workbook workbook = new XSSFWorkbook(is);
        Sheet sheet = workbook.getSheetAt(0);

        Row headerRow = sheet.getRow(0);
        String[] headers = {"logger", "ALL", "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL", "OFF"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.getCell(i);
            assertEquals(headers[i], cell.getStringCellValue());
        }

        LinkedHashMap<String, LinkedHashMap<String, Integer>> expectedStats = Persistency.generateStats();
        int rowIndex = 1;
        for (String loggerName : expectedStats.keySet()) {
            Row row = sheet.getRow(rowIndex++);
            assertEquals(loggerName, row.getCell(0).getStringCellValue());

            LinkedHashMap<String, Integer> loggerStats = expectedStats.get(loggerName);
            for (int i = 0; i < loggerStats.size(); i++) {
                assertEquals(loggerStats.get(headers[i + 1]).intValue(), (int) row.getCell(i + 1).getNumericCellValue());
            }
        }

        workbook.close();
    }

}
