package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPostLogs {

    private LogsServlet servlet;

    @BeforeEach
    public void setup() {
        this.servlet = new LogsServlet();
        try {
            deleteAllLogs();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
    public void testPostLog_withValidPayload() throws Exception {
        String postPayload = "{" +
                "\"id\":\"" + UUID.randomUUID() + "\"," +
                "\"message\":\"Test log message\"," +
                "\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                "\"thread\":\"main\"," +
                "\"logger\":\"com.example.TestLogger\"," +
                "\"level\":\"info\"," +
                "\"errorDetails\":\"test error\"" +
                "}";

        MockHttpServletRequest postRequest = new MockHttpServletRequest();
        postRequest.setMethod("POST");
        postRequest.setRequestURI("/restappender/logs");
        postRequest.setContentType("application/json");
        postRequest.setContent(postPayload.getBytes());

        MockHttpServletResponse postResponse = new MockHttpServletResponse();
        servlet.doPost(postRequest, postResponse);

        assertEquals(201, postResponse.getStatus());
        assertEquals(postPayload, postResponse.getContentAsString());
    }

    @Test
    public void testPostLog_withInvalidPayload() throws Exception {
        String postPayload = "{" +
                "\"id\":\"" + UUID.randomUUID() + "\"," +
                "\"message\":\"Test log message\"," +
                "\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                "\"thread\":\"main\"," +
                "\"logger\":\"\"," + // Invalid logger (empty)
                "\"level\":\"info\"," +
                "\"errorDetails\":\"test error\"" +
                "}";

        MockHttpServletRequest postRequest = new MockHttpServletRequest();
        postRequest.setMethod("POST");
        postRequest.setRequestURI("/restappender/logs");
        postRequest.setContentType("application/json");
        postRequest.setContent(postPayload.getBytes());

        MockHttpServletResponse postResponse = new MockHttpServletResponse();
        servlet.doPost(postRequest, postResponse);

        assertEquals(400, postResponse.getStatus());
        assertEquals("Invalid log entry", postResponse.getContentAsString());
    }

    @Test
    public void testPostLog_withDuplicateID() throws Exception {
        String id = UUID.randomUUID().toString();
        String postPayload1 = "{" +
                "\"id\":\"" + id + "\"," +
                "\"message\":\"Test log message 1\"," +
                "\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                "\"thread\":\"main\"," +
                "\"logger\":\"com.example.TestLogger1\"," +
                "\"level\":\"info\"," +
                "\"errorDetails\":\"test error 1\"" +
                "}";

        String postPayload2 = "{" +
                "\"id\":\"" + id + "\"," +
                "\"message\":\"Test log message 2\"," +
                "\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                "\"thread\":\"main\"," +
                "\"logger\":\"com.example.TestLogger2\"," +
                "\"level\":\"info\"," +
                "\"errorDetails\":\"test error 2\"" +
                "}";

        // Post first log
        MockHttpServletRequest postRequest1 = new MockHttpServletRequest();
        postRequest1.setMethod("POST");
        postRequest1.setRequestURI("/restappender/logs");
        postRequest1.setContentType("application/json");
        postRequest1.setContent(postPayload1.getBytes());
        MockHttpServletResponse postResponse1 = new MockHttpServletResponse();
        servlet.doPost(postRequest1, postResponse1);
        assertEquals(201, postResponse1.getStatus());

        // Post second log with duplicate ID
        MockHttpServletRequest postRequest2 = new MockHttpServletRequest();
        postRequest2.setMethod("POST");
        postRequest2.setRequestURI("/restappender/logs");
        postRequest2.setContentType("application/json");
        postRequest2.setContent(postPayload2.getBytes());
        MockHttpServletResponse postResponse2 = new MockHttpServletResponse();
        servlet.doPost(postRequest2, postResponse2);
        assertEquals(409, postResponse2.getStatus());
        assertEquals("Log entry with this ID already exists", postResponse2.getContentAsString());
    }




}
