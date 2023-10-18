package nz.ac.wgtn.swen301.restappender.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;


import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LogsServlet extends HttpServlet {
    private static final Gson gson = new GsonBuilder().create();


    public LogsServlet() {
        // Default constructor
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Return all log entries in JSON format
        int limit = (int) Double.parseDouble(req.getParameter("limit"));
        String level = req.getParameter("level");

        ArrayList<JsonObject> logs = new ArrayList<>();
        int i = 0;
        for(JsonObject jo : Persistency.DB){
            if(i <  limit){
                if(level.equalsIgnoreCase("all")){
                    logs.add(jo);
                } else {
                    if(level.equalsIgnoreCase(jo.get("level").getAsString())){
                        logs.add(jo);
                    }
                }
            }
            i++;
        }
        if(logs.isEmpty()){
            resp.getWriter().write("No logs found");
            return;
        }
        String jsonResponse = gson.toJson(logs);
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

    private boolean logEntryExists(JsonObject logEntry) {
        // Check existence directly on the JSON object
        String id = logEntry.get("id").getAsString();
        return Persistency.DB.stream()
                .anyMatch(existingLogEntry -> existingLogEntry.get("id").getAsString().equals(id));
    }

    private boolean validateLogEntry(JsonObject logEntry) {
        // Check if all required keys are present
        if (!logEntry.has("id") || !logEntry.has("message") || !logEntry.has("timestamp") ||
                !logEntry.has("thread") || !logEntry.has("logger") || !logEntry.has("level") ||
                !logEntry.has("errorDetails")) {
            return false;
        }

        // Ensure all properties have non-null and non-empty values
        if (logEntry.get("id").isJsonNull() || logEntry.get("id").getAsString().trim().isEmpty() ||
                logEntry.get("message").isJsonNull() || logEntry.get("message").getAsString().trim().isEmpty() ||
                logEntry.get("timestamp").isJsonNull() || logEntry.get("timestamp").getAsString().trim().isEmpty() ||
                logEntry.get("thread").isJsonNull() || logEntry.get("thread").getAsString().trim().isEmpty() ||
                logEntry.get("logger").isJsonNull() || logEntry.get("logger").getAsString().trim().isEmpty() ||
                logEntry.get("level").isJsonNull() || logEntry.get("level").getAsString().trim().isEmpty() ||
                logEntry.get("errorDetails").isJsonNull() || logEntry.get("errorDetails").getAsString().trim().isEmpty()) {
            return false;
        }

        // Validate ID as UUID
        try {
            UUID.fromString(logEntry.get("id").getAsString());
        } catch (IllegalArgumentException e) {
            return false; // Invalid UUID format
        }

        // Validate timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        sdf.setLenient(false); // This will make sure the date string strictly follows the pattern
        try {
            sdf.parse(logEntry.get("timestamp").getAsString());
        } catch (ParseException e) {
            return false; // Invalid timestamp format
        }

        // Validate logger level
        List<String> validLevels = Arrays.asList("all", "debug", "info", "warn", "error", "fatal", "trace", "off");
        if (!validLevels.contains(logEntry.get("level").getAsString().toLowerCase())) {
            return false; // Invalid logger level
        }

        return true;
    }

}
