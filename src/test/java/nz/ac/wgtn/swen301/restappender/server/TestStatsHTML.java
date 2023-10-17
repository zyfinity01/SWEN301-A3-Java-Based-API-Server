package nz.ac.wgtn.swen301.restappender.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStatsHTML {

    private StatsHTMLServlet servlet;

    @BeforeEach
    public void setup() {
        this.servlet = new StatsHTMLServlet();
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
    public void testGetStatsHTML() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/restappender/stats-html");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("text/html"));

        // Use jsoup to parse the HTML content
        Document doc = Jsoup.parse(response.getContentAsString());

        Element table = doc.select("table").first();
        assertTrue(table.hasAttr("border")); // Ensure the table has a border

        Element headerRow = table.select("tr").first();
        assertEquals("logger", headerRow.select("th").get(0).text());
        assertEquals("ALL", headerRow.select("th").get(1).text());
        assertEquals("TRACE", headerRow.select("th").get(2).text());

        Element firstDataRow = table.select("tr").get(1);
        assertEquals("com.example.Logger1", firstDataRow.select("td").get(0).text());
        // Add more assertions for other data rows and cells
    }

    @Test
    public void testExpectedHTML() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/restappender/stats-html");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("text/html"));

        Document doc = Jsoup.parse(response.getContentAsString());

        StringBuilder expectedHTML = new StringBuilder();
        expectedHTML.append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head><title>Logger Statistics</title></head>")
                .append("<body>")
                .append("<table border='1'>")
                .append("<tr><th>logger</th><th>ALL</th><th>TRACE</th><th>DEBUG</th><th>INFO</th><th>WARN</th><th>ERROR</th><th>FATAL</th><th>OFF</th></tr>");

        LinkedHashMap<String, LinkedHashMap<String, Integer>> stats = Persistency.generateStats();
        for (String loggerName : stats.keySet()) {
            expectedHTML.append("<tr>").append("<td>").append(loggerName).append("</td>");
            LinkedHashMap<String, Integer> loggerStats = stats.get(loggerName);
            for (String logLevel : loggerStats.keySet()) {
                expectedHTML.append("<td>").append(loggerStats.get(logLevel)).append("</td>");
            }
            expectedHTML.append("</tr>");
        }
        expectedHTML.append("</table>").append("</body>").append("</html>");

        Document expectedDoc = Jsoup.parse(expectedHTML.toString());

        Elements expectedRows = expectedDoc.select("table > tbody > tr");
        Elements actualRows = doc.select("table > tbody > tr");

        assertEquals(expectedRows.size(), actualRows.size());

        for (int i = 0; i < expectedRows.size(); i++) {
            Element expectedRow = expectedRows.get(i);
            Element actualRow = actualRows.get(i);

            Elements expectedCols = expectedRow.select("td, th");
            Elements actualCols = actualRow.select("td, th");

            assertEquals(expectedCols.size(), actualCols.size());

            for (int j = 0; j < expectedCols.size(); j++) {
                assertEquals(expectedCols.get(j).text(), actualCols.get(j).text());
            }
        }
    }
}
