package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStatsCSV {

    private static final Gson gson = new GsonBuilder().create();

    private StatsCSVServlet servlet;

    @BeforeEach
    public void setup() {
        this.servlet = new StatsCSVServlet();
        try {
            deleteAllLogs();
            addMockLogs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addMockLogs() throws Exception {
        String[] levels = { "debug", "info", "error", "trace", "warn", "fatal" };
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
    public void testGetStatsCSV() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/restappender/stats/csv");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().contains("text/csv"));

        // Here you can add more specific checks, such as parsing the CSV to ensure the counts for each logger and level are correct
        String csvContent = response.getContentAsString();
        String[] rows = csvContent.split("\n");
        // Assuming the first row is the header row
        for(int i = 0; i < rows.length; i++) {
            System.out.println(rows[i]);
        }
        assertTrue(rows[0].startsWith("logger\tALL\tTRACE\tDEBUG\tINFO\tWARN\tERROR\tFATAL\tOFF"));
        assertEquals(3, rows.length);
    }

    @Test
    public void testCSVOutput() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/stats/csv");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());

        String expectedOutput = "logger\tALL\tTRACE\tDEBUG\tINFO\tWARN\tERROR\tFATAL\tOFF\n" +
                "com.example.Logger1\t0\t0\t4\t0\t3\t3\t0\t0\t\n" +
                "com.example.Logger2\t0\t3\t0\t4\t0\t0\t3\t0\t\n";

        assertEquals(expectedOutput, response.getContentAsString());
    }


}
