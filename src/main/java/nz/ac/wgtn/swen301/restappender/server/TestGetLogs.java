package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGetLogs {

    private static final Gson gson = new GsonBuilder().create();


    private LogsServlet servlet;

    @BeforeEach
    public void setup() {
        this.servlet = new LogsServlet();
        try {
            deleteAllLogs();
            addMockLogs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addMockLogs() throws Exception {
        for (int i = 0; i < 10; i++) {
            String level = i % 3 == 0 ? "debug" : (i % 3 == 1 ? "info" : "error");

            String postPayload = "{" +
                    "\"id\":\"" + UUID.randomUUID() + "\"," +
                    "\"message\":\"Mock log message " + i + "\"," +
                    "\"timestamp\":\"" + LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                    "\"thread\":\"main\"," +
                    "\"logger\":\"com.example.Mock\"," +
                    "\"level\":\"" + level + "\"," +
                    "\"errorDetails\":\"mock error\"" +
                    "}";

            MockHttpServletRequest postRequest = new MockHttpServletRequest();
            postRequest.setMethod("POST");
            postRequest.setRequestURI("/restappender/logs");
            postRequest.setContentType("application/json");
            postRequest.setContent(postPayload.getBytes());

            MockHttpServletResponse postResponse = new MockHttpServletResponse();
            servlet.doPost(postRequest, postResponse);

            assertEquals(201, postResponse.getStatus());
        }
    }

    private void deleteAllLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("DELETE");
        request.setRequestURI("/restappender/logs");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doDelete(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testGetLogs_withValidLimitAndLevelAll() throws Exception {
        // Delete all logs before starting the test
        deleteAllLogs();

        // Define the expected JSON payload to send in the POST request
        String postPayload = "{" +
                "\"id\":\"343432dsf34432443s2323\"," +
                "\"message\":\"application started\"," +
                "\"timestamp\":\"04-05-2021 13:30:45\"," +
                "\"thread\":\"main\"," +
                "\"logger\":\"com.example.Foo\"," +
                "\"level\":\"debug\"," +
                "\"errorDetails\":\"string\"" +
                "}";

        MockHttpServletRequest postRequest = new MockHttpServletRequest();
        postRequest.setMethod("POST");
        postRequest.setRequestURI("/restappender/logs");
        postRequest.setContentType("application/json");
        postRequest.setContent(postPayload.getBytes());

        MockHttpServletResponse postResponse = new MockHttpServletResponse();
        servlet.doPost(postRequest, postResponse);

        assertEquals(201, postResponse.getStatus());

        // Create GET request to verify the log
        MockHttpServletRequest getRequest = new MockHttpServletRequest();
        getRequest.setMethod("GET");
        getRequest.setRequestURI("/logs");
        getRequest.setParameter("limit", "2");
        getRequest.setParameter("level", "all");

        MockHttpServletResponse getResponse = new MockHttpServletResponse();
        servlet.doGet(getRequest, getResponse);

        assertEquals(200, getResponse.getStatus());
        assertEquals("[" + postPayload + "]", getResponse.getContentAsString());
    }

    @Test
    public void testGetAllLogs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/logs");
        request.setParameter("limit", "2147483647");
        request.setParameter("level", "all");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        JsonArray jsonArray = JsonParser.parseString(response.getContentAsString()).getAsJsonArray();
        assertEquals(10, jsonArray.size());
    }

    @Test
    public void testGetLogsWithSpecificLevel() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/logs");
        request.setParameter("limit", "10");
        request.setParameter("level", "debug");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        List<JsonObject> logs = gson.fromJson(response.getContentAsString(), new TypeToken<List<JsonObject>>(){}.getType());
        assertTrue(logs.stream().allMatch(log -> "debug".equals(log.get("level").getAsString())));
    }


    @Test
    public void testGetLogsWithNoAvailableLogs() throws Exception {
        // To ensure there are no logs, explicitly clear them
        Persistency.DB.clear();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/logs");
        request.setParameter("limit", "10");
        request.setParameter("level", "all");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        assertEquals("No logs found", response.getContentAsString());
    }

    @Test
    public void testGetLogsWithInvalidLevel() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/logs");
        request.setParameter("limit", "10");
        request.setParameter("level", "invalid_level");

        MockHttpServletResponse response = new MockHttpServletResponse();
        servlet.doGet(request, response);

        assertEquals(200, response.getStatus());
        // Assuming no logs will be returned for an invalid level, hence response would be "No logs found"
        assertEquals("No logs found", response.getContentAsString());
    }


}
