package nz.ac.wgtn.swen301.restappender.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

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


}
