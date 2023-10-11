package nz.ac.wgtn.swen301.restappender.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TestDeleteLogs {

    private LogsServlet servlet;

    @BeforeEach
    public void setup() {
        this.servlet = new LogsServlet();
    }

    private void addLogEntry() throws Exception {
        String postPayload = "{" +
                "\"id\":\"" + UUID.randomUUID() + "\"," +
                "\"message\":\"Test log message for deletion\"," +
                "\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\"," +
                "\"thread\":\"main\"," +
                "\"logger\":\"com.example.TestLoggerForDelete\"," +
                "\"level\":\"info\"," +
                "\"errorDetails\":\"test error for deletion\"" +
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


    @Test
    public void testDeleteAllLogs() throws Exception {
        addLogEntry();

        assertFalse(Persistency.DB.isEmpty());

        MockHttpServletRequest deleteRequest = new MockHttpServletRequest();
        deleteRequest.setMethod("DELETE");
        deleteRequest.setRequestURI("/restappender/logs");

        MockHttpServletResponse deleteResponse = new MockHttpServletResponse();
        servlet.doDelete(deleteRequest, deleteResponse);

        assertEquals(200, deleteResponse.getStatus());
        assertEquals("{\"status\": \"All logs deleted\"}", deleteResponse.getContentAsString());

        // Ensure the logs are empty after the delete operation
        assertTrue(Persistency.DB.isEmpty());
    }



}
