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






}
