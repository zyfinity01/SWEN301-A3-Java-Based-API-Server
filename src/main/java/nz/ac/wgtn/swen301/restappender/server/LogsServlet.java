package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
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

        // Parse JSON to JsonObject
        JsonObject logEntry = gson.fromJson(jsonRequest, JsonObject.class);

        // Validate log entry
        if (!validateLogEntry(logEntry)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Invalid log entry");
            return;
        }

        // Check if log entry already exists
        if (logEntryExists(logEntry)) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write("Log entry with this ID already exists");
            return;
        }

        // Add logEntry to DB
        Persistency.DB.add(logEntry);

        // Send response (acknowledgment)
        resp.setStatus(HttpServletResponse.SC_CREATED);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(logEntry)); // Echo back the added log entry
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

    private boolean validateLogEntry(JsonObject logEntry) {
        // Perform validation directly on the JSON object
        return logEntry.has("id") && !logEntry.get("id").getAsString().isEmpty() &&
                logEntry.has("message") && !logEntry.get("message").getAsString().isEmpty() &&
                logEntry.has("timestamp") && !logEntry.get("timestamp").getAsString().isEmpty() &&
                logEntry.has("thread") && !logEntry.get("thread").getAsString().isEmpty() &&
                logEntry.has("logger") && !logEntry.get("logger").getAsString().isEmpty() &&
                logEntry.has("level") && !logEntry.get("level").getAsString().isEmpty();
    }

    private boolean logEntryExists(JsonObject logEntry) {
        // Check existence directly on the JSON object
        String id = logEntry.get("id").getAsString();
        return Persistency.DB.stream()
                .anyMatch(existingLogEntry -> existingLogEntry.get("id").getAsString().equals(id));
    }
}
