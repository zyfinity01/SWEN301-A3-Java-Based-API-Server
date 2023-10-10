package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class LogsServlet extends HttpServlet {
    private static final Gson gson = new GsonBuilder().create();

    public LogsServlet() {
        // Default constructor
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Return all log entries in JSON format
        String jsonResponse = gson.toJson(Persistency.DB);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Extract JSON string from request
        String jsonRequest = req.getReader().lines().collect(Collectors.joining());

        // Deserialize JSON to LogEvent object
        LogEvent logEvent = gson.fromJson(jsonRequest, Log4jLogEvent.class);

        // Add logEvent to DB
        Persistency.DB.add(logEvent);

        // Send response (acknowledgment)
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(logEvent)); // Echo back the added log event
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Clear all logs from DB
        Persistency.DB.clear();

        // Send response (acknowledgment)
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write("{\"status\": \"All logs deleted\"}");
    }
}
