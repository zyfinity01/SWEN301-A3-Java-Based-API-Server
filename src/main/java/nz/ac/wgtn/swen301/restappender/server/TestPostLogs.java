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


}
